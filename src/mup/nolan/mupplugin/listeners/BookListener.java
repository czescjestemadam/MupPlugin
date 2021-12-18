package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.modules.chatpatrol.ChatPatrolModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;

public class BookListener implements Listener
{
	private final MupPlugin mupPlugin;
	private final ModuleManager mm;

	public BookListener(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
		this.mm = mupPlugin.getModuleManager();
	}

	@EventHandler
	private void onBook(PlayerEditBookEvent e)
	{
		((ChatPatrolModule)mm.getModule("chatpatrol")).onBook(e);
	}
}
