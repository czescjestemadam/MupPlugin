package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.gallery.GalleryModule;
import mup.nolan.mupplugin.modules.ItemsortModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

public class InventoryListener implements Listener
{
	private final MupPlugin mupPlugin;

	public InventoryListener(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
	}

	@EventHandler
	private void onClose(InventoryCloseEvent e)
	{
		((GalleryModule)mupPlugin.getModuleManager().getModule("gallery")).onClose(e);

		((ItemsortModule)mupPlugin.getModuleManager().getModule("itemsort")).onChest(e);
	}

	@EventHandler
	private void onClick(InventoryClickEvent e)
	{
		((GalleryModule)mm.getModule("gallery")).onClick(e);
		((ChatPatrolModule)mm.getModule("chatpatrol")).onAnvil(e);
	}
}
