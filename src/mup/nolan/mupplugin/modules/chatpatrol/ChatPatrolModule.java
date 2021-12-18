package mup.nolan.mupplugin.modules.chatpatrol;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.hooks.VaultHook;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.utils.CommandUtils;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;

import java.util.*;
import java.util.function.Consumer;

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
			final String line = e.getLine(i);
			final String filtered = checkCategories(line);
			if (filtered == null)
			{
				e.setCancelled(true);
				return;
			}
			else if (!line.equals(filtered))
			{
				e.setLine(i, filtered);
			}
		}

		final String joined = String.join(" ", e.getLines());
		final String filtered = checkCategories(joined);
		if (!joined.equals(filtered))
			e.setCancelled(true);
	}

	public void onBook(PlayerEditBookEvent e)
	{

	}

	public void onCommand(PlayerCommandPreprocessEvent e)
	{

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

		final int maxCaps = cfg.getInt("caps.max-letters");
		int caps = 0;
		for (char c : e.getMessage().toCharArray())
		{
			if (Character.isUpperCase(c))
				caps++;
			if (caps > maxCaps)
				break;
		}

		if (caps < maxCaps)
			return;

		final StringBuilder msg = new StringBuilder();
		for (String s : e.getMessage().split(" "))
		{
			if (Bukkit.getPlayerExact(s) != null)
				msg.append(s);
			else
				msg.append(s.toLowerCase());
			msg.append(" ");
		}
		e.setMessage(msg.toString().trim());
	}

	private void checkFlood(AsyncPlayerChatEvent e)
	{

	}

	private String checkCategories(String str)
	{

		return str;
	}

	private void checkCategory(String content, String category)
	{

	}
}
