package mup.nolan.mupplugin.modules.chatpatrol;

import com.google.common.primitives.Chars;
import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.hooks.VaultHook;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.utils.CommandUtils;
import mup.nolan.mupplugin.utils.NetUtils;
import mup.nolan.mupplugin.utils.Resrc;
import mup.nolan.mupplugin.utils.StrUtils;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatPatrolModule extends Module
{
	private final Queue<ChatPatrolLogMessage> chatLog = new LinkedList<>();

	public ChatPatrolModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "chatpatrol");
	}

	@Override
	public void onDisable()
	{
		chatLog.clear();
	}

	public void onChat(AsyncPlayerChatEvent e)
	{
		if (!isEnabled())
			return;

		checkCooldown(e);
		checkSpam(e);
		checkCaps(e);
		checkFlood(e);

		final Resrc<String> msg = new Resrc<>(e.getMessage());
		checkCategories("chat", e.getPlayer(), msg, e);
		e.setMessage(msg.get());

		if (!e.isCancelled())
		{
			chatLog.add(new ChatPatrolLogMessage(e.getPlayer(), e.getMessage()));
			if (chatLog.size() >= cfg().getInt("spam.chatlog-size"))
				chatLog.remove();
		}
	}

	public void onSign(SignChangeEvent e)
	{
		for (int i = 0; i < 4; i++)
		{
			final Resrc<String> line = new Resrc<>(e.getLine(i));
			checkCategories("sign", e.getPlayer(), line, e);
			if (e.isCancelled())
				return;
			e.setLine(i, line.get());
		}
	}

	public void onBook(PlayerEditBookEvent e)
	{
		final BookMeta m = e.getNewBookMeta();
		for (int i = 1; i <= m.getPageCount(); i++)
		{
			final Resrc<String> page = new Resrc<>(m.getPage(i));
			checkCategories("book", e.getPlayer(), page, e);
			if (e.isCancelled())
			{
				m.setPage(i, "");
				e.setCancelled(false);
			}
			else
				m.setPage(i, page.get());
		}
		e.setNewBookMeta(m);
		Bukkit.getScheduler().scheduleSyncDelayedTask(mup(), () -> e.getPlayer().updateInventory(), 1);
	}

	public void onCommand(PlayerCommandPreprocessEvent e)
	{
		final String msg = e.getMessage().substring(1);
		final int idx = msg.indexOf(" ");
		if (idx < 0)
			return;
		final String cmd = msg.substring(0, idx);
		final Resrc<String> args = new Resrc<>(msg.substring(msg.indexOf(" ")).trim());
		checkCategories("command:" + cmd, e.getPlayer(), args, e);
		e.setMessage("/" + cmd + " " + args.get());
	}

	private void checkCooldown(AsyncPlayerChatEvent e)
	{
		if (e.getPlayer().hasPermission("mup.chatpatrol.exempt.cooldown"))
			return;

		final ChatPatrolLogMessage lastMessage = chatLog.stream().filter(m -> m.sender == e.getPlayer()).max(Comparator.comparingLong(v -> v.timestamp)).orElse(null);
		if (lastMessage == null)
			return;

		final String valName = (e.getMessage().equalsIgnoreCase(lastMessage.content) ? "repeat-" : "") + "time";
		final int cooldown = cfg().getInt("cooldown." + VaultHook.getPerms().getPrimaryGroup(e.getPlayer()).toLowerCase() + "." + valName, cfg().getInt("cooldown.default." + valName));

		final int lastDiff = (int)(System.currentTimeMillis() - lastMessage.timestamp);
		if (lastDiff > cooldown)
			return;

		e.setCancelled(true);
		e.getPlayer().sendMessage(cfg().getStringF("messages.cooldown").replaceAll("\\{}", StrUtils.roundNum((cooldown - (double)lastDiff) / 1000, 1)));
	}

	private void checkSpam(AsyncPlayerChatEvent e)
	{
		if (e.isCancelled() || e.getPlayer().hasPermission("mup.chatpatrol.exempt.spam"))
			return;

		final List<ChatPatrolLogMessage> messages = chatLog.stream().filter(m -> m.sender == e.getPlayer() && !m.warned).toList();

		final int minWords = cfg().getInt("spam.matching.min-words");
		final int minPerc = cfg().getInt("spam.matching.min-percent");

		final List<String> matching = new ArrayList<>(List.of(e.getMessage()));
		for (ChatPatrolLogMessage msg : messages)
		{
			if (e.getMessage().equalsIgnoreCase(msg.content))
			{
				matching.add(msg.content);
				continue;
			}

			final String[] msgArr = e.getMessage().toLowerCase().split(" ");
			final String[] logArr = msg.content.toLowerCase().split(" ");

			if (msgArr.length < minWords)
				break;

			final List<String> from = Arrays.asList(msgArr.length > logArr.length ? logArr : msgArr); // smaller
			final List<String> toCheck = Arrays.asList(msgArr.length > logArr.length ? msgArr : logArr); // bigger

			int matchingWords = 0;
			for (String fromStr : from)
			{
				if (toCheck.contains(fromStr))
					matchingWords++;
			}

			if (matchingWords >= minWords && 100 * matchingWords / from.size() >= minPerc)
				matching.add(msg.content);
		}

		if (matching.size() < cfg().getInt("spam.max"))
			return;

		final Consumer<String> execCommand = cmd -> {
			if (cmd != null && !cmd.equalsIgnoreCase("none"))
			{
				e.getPlayer().sendMessage(cfg().getStringF("messages.spam"));
				CommandUtils.execAsync(Bukkit.getConsoleSender(), cmd.replaceAll("\\{}", e.getPlayer().getName()));
				chatLog.stream().filter(m -> m.sender == e.getPlayer()).forEach(m -> m.warned = true);
			}
		};

		for (String filter : cfg().getStringList("spam.cheat-spammer.blacklist"))
		{
			if (matching.stream().allMatch(m -> m.matches(filter)))
			{
				if (cfg().getString("spam.cheat-spammer.action").equalsIgnoreCase("cancel"))
					e.setCancelled(true);
				if (!e.getPlayer().hasPermission("mup.chatpatrol.exempt.punish"))
					execCommand.accept(cfg().getString("spam.cheat-spammer.command"));
				return;
			}
		}

		if (cfg().getString("spam.action").equalsIgnoreCase("cancel"))
			e.setCancelled(true);
		if (!e.getPlayer().hasPermission("mup.chatpatrol.exempt.punish"))
			execCommand.accept(cfg().getString("spam.command"));
	}

	private void checkCaps(AsyncPlayerChatEvent e)
	{
		if (e.isCancelled() || e.getPlayer().hasPermission("mup.chatpatrol.exempt.caps"))
			return;

		String msg = e.getMessage();
		for (Player p : Bukkit.getOnlinePlayers())
			msg = msg.replaceAll(p.getName(), "");

		if (msg.trim().isEmpty())
			return;

		final int maxCaps = cfg().getInt("caps.max-letters");
		int caps = 0;
		for (char c : msg.toCharArray())
		{
			if (Character.isUpperCase(c))
				caps++;
			if (caps > maxCaps)
				break;
		}

		if (caps < maxCaps)
			return;

		final StringBuilder low = new StringBuilder();
		for (String s : e.getMessage().split(" "))
		{
			if (Bukkit.getPlayerExact(s) != null)
				low.append(s);
			else
				low.append(s.toLowerCase());
			low.append(" ");
		}
		e.setMessage(low.toString().trim());
		e.getPlayer().sendMessage(cfg().getStringF("messages.caps"));
	}

	private void checkFlood(AsyncPlayerChatEvent e)
	{
		if (e.isCancelled())
			return;

		final int maxRepeats = cfg().getInt("flood.max-repeats");
		final int reduceTo = cfg().getInt("flood.reduce-to");
		final boolean allowUsernames = cfg().getBool("flood.allow-usernames");

		boolean command = false;
		final String[] msgArr = e.getMessage().split(" ");
		for (int argIdx = 0; argIdx < msgArr.length; argIdx++)
		{
			if (msgArr[argIdx].length() <= maxRepeats)
				continue;

			final List<Character> arg = new ArrayList<>(Chars.asList(msgArr[argIdx].toCharArray()));
			if (allowUsernames && Bukkit.getPlayerExact(msgArr[argIdx]) != null)
				continue;

			for (int letterIdx = 0; letterIdx < arg.size(); letterIdx++)
			{
				final char letter = arg.get(letterIdx);
				int matching = 0;

				for (int nextLetterIdx = letterIdx; nextLetterIdx < arg.size() - 1; nextLetterIdx++)
				{
					if (letter == arg.get(nextLetterIdx))
						matching++;
					else
						break;
				}

				if (maxRepeats > matching)
					continue;

				for (int removeIdx = 0; removeIdx < matching - reduceTo; removeIdx++)
				{
					if (arg.get(letterIdx) != letter)
						break;
					arg.remove(letterIdx);
				}

				command = true;
			}

			msgArr[argIdx] = arg.stream().map(String::valueOf).collect(Collectors.joining());
		}

		if (!command)
			return;

		final String action = cfg().getString("flood.action");
		if (action.equalsIgnoreCase("replace"))
			e.setMessage(String.join(" ", msgArr));
		else if (action.equalsIgnoreCase("cancel"))
			e.setCancelled(true);
		e.getPlayer().sendMessage(cfg().getStringF("messages.flood"));
		final String cmd = cfg().getString("flood.command");
		if (cmd != null && !cmd.equalsIgnoreCase("none") && !e.getPlayer().hasPermission("mup.chatpatrol.exempt.punish"))
			CommandUtils.execAsync(Bukkit.getConsoleSender(), cfg().getString("flood.command"));
	}

	private void checkCategories(String from, Player player, Resrc<String> content, Cancellable event)
	{
		if (event.isCancelled())
			return;

		for (String category : cfg().list("categories"))
		{
			if (!cfg().getStringList("categories." + category + ".apply-to").contains(from))
				continue;

			final String original = content.get();
			checkCategory(category, player, content, event);
			if (event.isCancelled() || !original.equalsIgnoreCase(content.get()))
				notify(from, category, player, original);
		}
	}

	private void checkCategory(String category, Player player, Resrc<String> content, Cancellable event)
	{
		if (event.isCancelled())
			return;

		final String replacement = cfg().getString("replacement");

		boolean dnsAction = false;
		if (cfg().has("categories." + category + ".dns-lookup"))
		{
			final Matcher m = Pattern.compile(cfg().getString("categories." + category + ".dns-lookup.format"), Pattern.CASE_INSENSITIVE).matcher(content.get());
			while (m.find())
			{
				final String group = m.group().replaceAll("[ ,-]+", ".");
				if (NetUtils.dnsLookup(group) != null)
				{
					final String action = cfg().getString("categories." + category + ".dns-lookup.action");
					if (action.equalsIgnoreCase("cancel"))
						event.setCancelled(true);
					else if (action.equalsIgnoreCase("replace") && !player.hasPermission("mup.chatpatrol.exempt.replace"))
						content.set(content.get().replace(group, replacement));
					final String cmd = cfg().getString("categories." + category + ".dns-lookup.command");
					if (cmd != null && !cmd.equalsIgnoreCase("none"))
					{
						dnsAction = true;
						if (!player.hasPermission("mup.chatpatrol.exempt.punish"))
							CommandUtils.execAsync(Bukkit.getConsoleSender(), cmd.replaceAll("\\{}", player.getName()));
					}
				}
			}
		}

		final List<String> ignores = new ArrayList<>();
		for (String whiteString : cfg().getStringList("categories." + category + ".whitelist"))
		{
			final Matcher m = Pattern.compile(whiteString, Pattern.CASE_INSENSITIVE).matcher(content.get());
			while (m.find())
				ignores.add(m.group());
		}

		final List<String> matches = new ArrayList<>();
		for (String blackString : cfg().getStringList("categories." + category + ".blacklist"))
		{
			final Matcher m = Pattern.compile("(" + blackString + ")+", Pattern.CASE_INSENSITIVE).matcher(content.get());
			while (m.find())
			{
				final String group = m.group();
				boolean contains = false;
				for (String s : ignores)
				{
					if (s.contains(group))
					{
						contains = true;
						break;
					}
				}
				if (!contains)
					matches.add(group);
			}
		}

		if (matches.isEmpty())
			return;

		player.sendMessage(cfg().getStringF("categories." + category + ".message"));
		final String action = cfg().getString("categories." + category + ".action");
		if (action.equalsIgnoreCase("cancel"))
			event.setCancelled(true);
		else if (action.equalsIgnoreCase("replace") && !player.hasPermission("mup.chatpatrol.exempt.replace"))
			for (String match : matches)
				content.set(content.get().replace(match, replacement));
		final String cmd = cfg().getString("categories." + category + ".command");
		if (cmd != null && !cmd.equalsIgnoreCase("none") && !dnsAction && !player.hasPermission("mup.chatpatrol.exempt.punish"))
			CommandUtils.execAsync(Bukkit.getConsoleSender(), cmd.replaceAll("\\{}", player.getName()));
	}

	private void notify(String from, String category, Player player, String original)
	{
		final Function<String, String> placeholders = str -> str.replaceAll("\\{player}", player.getName())
				.replaceAll("\\{category}", category)
				.replaceAll("\\{source}", from)
				.replaceAll("\\{original}", original);

		final String msg = placeholders.apply(cfg().getStringF("notification.message"));
		final String hover = placeholders.apply(cfg().getStringF("notification.hover"));

		final TextComponent notification = new TextComponent(msg);
		notification.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, List.of(new Text(hover))));

		MupPlugin.log().info(msg + "§r; " + hover);
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (p.hasPermission("mup.chatpatrol.notify"))
				p.spigot().sendMessage(notification);
		}
	}
}
