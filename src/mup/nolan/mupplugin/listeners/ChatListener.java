package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.modules.chatpatrol.ChatPatrolModule;
import mup.nolan.mupplugin.modules.discord.DiscordModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ChatListener implements Listener
{
	private final MupPlugin mupPlugin;
	private final ModuleManager mm;

	public ChatListener(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
		mm = mupPlugin.getModuleManager();
	}

	@EventHandler
	private void onChat(AsyncPlayerChatEvent e)
	{
		((ChatPatrolModule)mm.getModule("chatpatrol")).onChat(e);
		((DiscordModule)mm.getModule("discord")).onChat(e);
	}

	@EventHandler
	private void onCommand(PlayerCommandPreprocessEvent e)
	{
		((ChatPatrolModule)mm.getModule("chatpatrol")).onCommand(e);
	}
}
