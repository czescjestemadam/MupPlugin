package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.antiafk.AntiafkModule;
import mup.nolan.mupplugin.modules.gallery.GalleryModule;
import mup.nolan.mupplugin.modules.ItemsortModule;
import mup.nolan.mupplugin.modules.ModuleManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener
{
	private final MupPlugin mupPlugin;
	private final ModuleManager mm;

	public ConnectionListener(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
		mm = mupPlugin.getModuleManager();
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent e)
	{
		((AntiafkModule)mm.getModule("antiafk")).onJoin(e.getPlayer());
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e)
	{
		((ItemsortModule)mm.getModule("itemsort")).clearReminder(e.getPlayer());
		((GalleryModule)mm.getModule("gallery")).clearReminder(e.getPlayer());
		((AntiafkModule)mm.getModule("antiafk")).onQuit(e.getPlayer());
	}
}
