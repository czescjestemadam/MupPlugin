package mup.nolan.mupplugin.modules.chatpatrol;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.modules.Module;
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
	public void onEnable()
	{

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
		final ChatPatrolLogMessage lastMessage = chatLog.stream().filter(m -> m.sender == e.getPlayer()).max(Comparator.comparingLong(v -> v.timestamp)).orElse(null);
		if (lastMessage == null)
			return;


	}

	private void checkSpam(AsyncPlayerChatEvent e)
	{

	}

	private void checkCaps(AsyncPlayerChatEvent e)
	{

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
