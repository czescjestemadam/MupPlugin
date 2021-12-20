package mup.nolan.mupplugin.modules.chatpatrol;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.hooks.VaultHook;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.utils.CommandUtils;
import mup.nolan.mupplugin.utils.NetUtils;
import mup.nolan.mupplugin.utils.Resrc;
import mup.nolan.mupplugin.utils.StrUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatPatrolModule extends Module
{
	private final Config cfg;
	private final Queue<ChatPatrolLogMessage> chatLog = new LinkedList<>();

	public ChatPatrolModule(MupPlugin mupPlugin)
	{
        super(mupPlugin, "chatpatrol");
		cfg = mupPlugin.getConfigManager().getConfig("chatpatrol");
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

		final Resrc<String> msg = new Resrc<>(e.getMessage());
		checkCategories("chat", e.getPlayer(), msg, e);
		e.setMessage(msg.get());

		checkFlood(e);
		if (!e.isCancelled())
		{
			chatLog.add(new ChatPatrolLogMessage(e.getPlayer(), e.getMessage()));
			if (chatLog.size() >= cfg.getInt("spam.chatlog-size"))
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
	}

	public void onCommand(PlayerCommandPreprocessEvent e)
	{
		final String msg = e.getMessage().substring(1);
		final String cmd = msg.substring(0, msg.indexOf(" "));
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
		final int cooldown = cfg.getInt("cooldown." + VaultHook.getPerms().getPrimaryGroup(e.getPlayer()).toLowerCase() + "." + valName, cfg.getInt("cooldown.default." + valName));

		final int lastDiff = (int)(System.currentTimeMillis() - lastMessage.timestamp);
		if (lastDiff > cooldown)
			return;

		e.setCancelled(true);
		e.getPlayer().sendMessage(cfg.getStringF("messages.cooldown").replaceAll("\\{}", StrUtils.roundNum((cooldown - (double)lastDiff) / 1000, 1)));
	}

	private void checkSpam(AsyncPlayerChatEvent e)
	{
		if (e.isCancelled() || e.getPlayer().hasPermission("mup.chatpatrol.exempt.spam"))
			return;

		final List<ChatPatrolLogMessage> messages = chatLog.stream().filter(m -> m.sender == e.getPlayer() && !m.warned).toList();

		final int minWords = cfg.getInt("spam.matching.min-words");
		final int minPerc = cfg.getInt("spam.matching.min-percent");

		final List<String> matching = new ArrayList<>(Arrays.asList(e.getMessage()));
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

		if (matching.size() < cfg.getInt("spam.max"))
			return;

		final Consumer<String> execCommand = cmd -> {
			if (cmd != null && !cmd.equalsIgnoreCase("none"))
			{
				e.getPlayer().sendMessage(cfg.getStringF("messages.spam"));
				CommandUtils.execAsync(Bukkit.getConsoleSender(), cmd.replaceAll("\\{}", e.getPlayer().getName()));
				chatLog.stream().filter(m -> m.sender == e.getPlayer()).forEach(m -> m.warned = true);
			}
		};

		for (String filter : cfg.getStringList("spam.cheat-spammer.blacklist"))
		{
			if (matching.stream().allMatch(m -> m.matches(filter)))
			{
				if (cfg.getString("spam.cheat-spammer.action").equalsIgnoreCase("cancel"))
					e.setCancelled(true);
				execCommand.accept(cfg.getString("spam.cheat-spammer.command"));
				return;
			}
		}

		if (cfg.getString("spam.action").equalsIgnoreCase("cancel"))
			e.setCancelled(true);
		execCommand.accept(cfg.getString("spam.command"));
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

		final int maxCaps = cfg.getInt("caps.max-letters");
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
		e.getPlayer().sendMessage(cfg.getStringF("messages.caps"));
	}

	private void checkFlood(AsyncPlayerChatEvent e)
	{

	}

	private void checkCategories(String from, Player player, Resrc<String> content, Cancellable event)
	{
		if (event.isCancelled())
			return;

		for (String category : cfg.list("categories"))
		{
			if (cfg.getStringList("categories." + category + ".apply-to").contains(from))
				checkCategory(category, player, content, event);
		}
	}

	private void checkCategory(String category, Player player, Resrc<String> content, Cancellable event)
	{
		if (event.isCancelled())
			return;

		final String replacement = cfg.getString("replacement");

		boolean dnsAction = false;
		if (cfg.has("categories." + category + ".dns-lookup"))
		{
			final Matcher m = Pattern.compile(cfg.getString("categories." + category + ".dns-lookup.format"), Pattern.CASE_INSENSITIVE).matcher(content.get());
			while (m.find())
			{
				final String group = m.group();
				System.out.println("group: " + group);

				if (NetUtils.dnsLookup(group) != null)
				{
					System.out.println("dns found addr");
					final String action = cfg.getString("categories." + category + ".dns-lookup.action");
					if (action.equalsIgnoreCase("cancel"))
						event.setCancelled(true);
					else if (action.equalsIgnoreCase("replace"))
						content.set(content.get().replace(group, replacement));
					final String cmd = cfg.getString("categories." + category + ".dns-lookup.command");
					if (cmd != null && !cmd.equalsIgnoreCase("none"))
					{
						dnsAction = true;
						CommandUtils.execAsync(Bukkit.getConsoleSender(), cmd.replaceAll("\\{}", player.getName()));
					}
				}
			}
		}

		final List<String> ignores = new ArrayList<>();
		for (String whiteString : cfg.getStringList("categories." + category + ".whitelist"))
		{
			final Matcher m = Pattern.compile(whiteString, Pattern.CASE_INSENSITIVE).matcher(content.get());
			while (m.find())
				ignores.add(m.group());
		}

		final List<String> matches = new ArrayList<>();
		for (String blackString : cfg.getStringList("categories." + category + ".blacklist"))
		{
			final Matcher m = Pattern.compile(blackString, Pattern.CASE_INSENSITIVE).matcher(content.get());
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

		player.sendMessage(cfg.getStringF("categories." + category + ".message"));
		final String action = cfg.getString("categories." + category + ".action");
		if (action.equalsIgnoreCase("cancel"))
			event.setCancelled(true);
		else if (action.equalsIgnoreCase("replace"))
			for (String match : matches)
				content.set(content.get().replace(match, replacement));
		final String cmd = cfg.getString("categories." + category + ".command");
		if (cmd != null && !cmd.equalsIgnoreCase("none") && !dnsAction)
			CommandUtils.execAsync(Bukkit.getConsoleSender(), cmd.replaceAll("\\{}", player.getName()));
	}
}
