package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.modules.chatpatrol.ChatPatrolModule;
import mup.nolan.mupplugin.modules.gallery.GalleryModule;
import mup.nolan.mupplugin.modules.ItemsortModule;
import mup.nolan.mupplugin.modules.AnvilsModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

public class InventoryListener implements Listener
{
	private final MupPlugin mupPlugin;
	private final ModuleManager mm;

	public InventoryListener(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
		mm = mupPlugin.getModuleManager();
	}

	@EventHandler
	private void onClose(InventoryCloseEvent e)
	{
		((GalleryModule)mm.getModule("gallery")).onClose(e);
		((ItemsortModule)mm.getModule("itemsort")).onChest(e);
		((AnvilsModule)mm.getModule("anvils")).onAnvil(e);
	}

	@EventHandler
	private void onClick(InventoryClickEvent e)
	{
		((GalleryModule)mm.getModule("gallery")).onClick(e);
		((ChatPatrolModule)mm.getModule("chatpatrol")).onAnvil(e);
	}
}
