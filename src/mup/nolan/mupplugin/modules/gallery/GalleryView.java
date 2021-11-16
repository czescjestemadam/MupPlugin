package mup.nolan.mupplugin.modules.gallery;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.db.GalleryRow;
import mup.nolan.mupplugin.db.GalleryUserdataRow;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GalleryView
{
	private final List<GalleryRow> items;
	private final GalleryUserdataRow userdata;
	private final Inventory inv;
	private final boolean editmode;
	private int page;

	GalleryView(List<GalleryRow> items, GalleryUserdataRow userdata, Inventory inv, int page, boolean editmode)
	{
		this.items = items;
		this.userdata = userdata;
		this.inv = inv;
		this.page = page;
		this.editmode = editmode;
	}

	public void onClick(InventoryClickEvent e)
	{


		if (!editmode) // if !editmode cancel item move
			e.setCancelled(true);
		else if (e.getSlot() < 9 || e.getSlot() > 44 || e.getSlot() % 9 == 0 || e.getSlot() % 9 == 8) // else cancel only border
			e.setCancelled(true);
	}

	public void onClose(InventoryCloseEvent e)
	{
		final Player p = (Player)e.getPlayer();

		e.getView().setCursor(null);
		Bukkit.getScheduler().scheduleSyncDelayedTask(MupPlugin.get(), p::updateInventory, 1);

		if (!editmode)
			return;

		final List<ItemStack> invItems = new ArrayList<>();

		for (int i = 0; i < inv.getSize(); i++)
		{
			if (i > 9 && i < 44 && i % 9 != 0 && i % 9 != 8)
				invItems.add(inv.getStorageContents()[i]);
		}


	}
}
