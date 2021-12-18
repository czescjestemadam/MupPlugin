package mup.nolan.mupplugin.modules.chatpatrol;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.hooks.VaultHook;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;

import java.util.*;

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
			if (chatLog.size() > cfg.getInt("spam.messages.chatlog-size"))
			chatLog.add(new ChatPatrolLogMessage(e.getPlayer(), e.getMessage()));
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
