package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.gallery.GalleryModule;
import mup.nolan.mupplugin.modules.ItemsortModule;
import mup.nolan.mupplugin.modules.ModuleManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener
{
	private final MupPlugin mupPlugin;

	public ConnectionListener(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e)
	{
		final ModuleManager mm = mupPlugin.getModuleManager();

		((ItemsortModule)mm.getModule("itemsort")).clearReminder(e.getPlayer());
		((GalleryModule)mm.getModule("gallery")).clearReminder(e.getPlayer());
	}
}
