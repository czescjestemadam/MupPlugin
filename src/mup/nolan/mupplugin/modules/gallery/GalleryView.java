package mup.nolan.mupplugin.modules.gallery;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.db.GalleryRow;
import mup.nolan.mupplugin.db.GalleryUserdataRow;
import mup.nolan.mupplugin.utils.ItemBuilder;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class GalleryView
{
	private static final Config cfg = MupPlugin.get().getConfigManager().getConfig("gallery");

	private final Player player;
	private final OfflinePlayer owner;
	private final boolean editmode;
	private final List<GalleryRow> items;
	private final GalleryUserdataRow userdata;
	private final Inventory inv;
	private final List<List<ItemStack>> editRawItems = new ArrayList<>();
	private final int borderMenuPos = cfg.getInt("gui-items.border-buymenu-pos");
	private final int slotMenuPos = cfg.getInt("gui-items.slot-buymenu-pos");
	private GalleryViewType type = GalleryViewType.MAIN;
	private int page = 0;
	private int allPages;

	public GalleryView(Player player, OfflinePlayer owner, boolean editmode, List<GalleryRow> items, GalleryUserdataRow userdata)
	{
		this.player = player;
		this.owner = owner;
		this.editmode = editmode;
		this.items = items;
		this.userdata = userdata == null ? new GalleryUserdataRow(-1, owner, 0, "", cfg.getMaterial("gui-items.default-border"), null, null) : userdata;

		allPages = items.size() / 28 + (items.size() % 28 > 0 ? 1 : 0);

		if (editmode) // add paged rawItems
		{
			for (int i = 0; i < allPages; i++)
				editRawItems.add(items.subList(Math.min(items.size(), i * 28), Math.min(items.size(), i * 28 + 27)).stream().map(GalleryRow::getItem).collect(Collectors.toList()));
		}

		// create inventory
		final String winName = cfg.getString("messages.gui.win-name." + (editmode ? "edit-" : "") + (owner == player ? "own" : "other"));
		inv = Bukkit.createInventory(null, 54, StrUtils.replaceColors(winName.replace("{}", owner.getName())));

		renderPage();

		player.openInventory(inv);
	}

	public void onClick(InventoryClickEvent e)
	{
		final int i = e.getSlot();

		if (i == 48)
			prevPage();
		else if (i == 50)
			nextPage();
		else if (i == borderMenuPos)
			borderMenu();
		else if (i == slotMenuPos)
			slotMenu();

		if (!editmode) // if !editmode cancel item move
			e.setCancelled(true);
		else if (isBorder(i))
			e.setCancelled(true);
	}

	public void onClose(InventoryCloseEvent e)
	{
		final Player p = (Player)e.getPlayer();

		e.getView().setCursor(null);
		Bukkit.getScheduler().scheduleSyncDelayedTask(MupPlugin.get(), p::updateInventory, 1);

		if (!editmode)
			return;

		final List<ItemStack> invItems = readInvContents(new ArrayList<>());
		if (editRawItems.isEmpty())
			return;

		editRawItems.set(page, invItems);
		saveItems();
	}

	private void renderPage()
	{
		// prepare items
		final ItemStack border = new ItemBuilder(userdata.getCurrentBorder()).withName("§").build();
		final ItemStack lockedSlot = new ItemBuilder(cfg.getMaterial("gui-items.locked-slot")).withName(cfg.getStringF("messages.gui.locked-slot")).build();
		final ItemStack prevPage = new ItemBuilder(cfg.getMaterial("gui-items.prev-page")).withName(cfg.getStringF("messages.gui.prev-page")).build();
		final ItemStack nextPage = new ItemBuilder(cfg.getMaterial("gui-items.next-page")).withName(cfg.getStringF("messages.gui.next-page")).build();
		final ItemStack borderMenu = new ItemBuilder(cfg.getMaterial("gui-items.border-buymenu")).withName(cfg.getStringF("messages.gui.border-buymenu")).build();
		final ItemStack slotMenu = new ItemBuilder(cfg.getMaterial("gui-items.slot-buymenu")).withName(cfg.getStringF("messages.gui.slot-buymenu")).build();

		final List<String> infols = cfg.getStringList("messages.gui.info");
		infols.replaceAll(StrUtils::replaceColors);
		final ItemStack info = new ItemBuilder(cfg.getMaterial("gui-items.info")).withName(infols.remove(0)).withLore(infols).build();

		// set items (per page = 28)
		final Iterator<GalleryRow> it = items.subList(Math.min(items.size(), page * 28), Math.min(items.size(), page * 28 + 27)).iterator();
		for (int i = 0; i < inv.getSize(); i++)
		{
			ItemStack is = lockedSlot;

			if (i == 48)
				is = prevPage;
			else if (i == 49)
				is = info;
			else if (i == 50)
				is = nextPage;

				// if editmode add border menu and buy slots buttons
			else if (i == borderMenuPos && editmode)
				is = borderMenu;
			else if (i == slotMenuPos && editmode)
				is = slotMenu;

			else if (isBorder(i))
				is = border;
			else if (it.hasNext())
				is = it.next().getItem(); // if !editmode add date lockId

			inv.setItem(i, is);
		}
	}

	private void prevPage()
	{
		System.out.println("prevPage");
	}

	private void nextPage()
	{
		System.out.println("nextPage");
	}

	private void borderMenu()
	{
		System.out.println("borderMenu");

		type = GalleryViewType.BORDER;
	}

	private void slotMenu()
	{
		System.out.println("slotMenu");

		type = GalleryViewType.SLOT;
	}

	private List<ItemStack> readInvContents(List<ItemStack> items)
	{
		final ItemStack[] invItems = inv.getStorageContents();
		final ItemStack border = new ItemBuilder(userdata.getCurrentBorder()).withName("§").build();

		for (int i = 0; i < inv.getSize(); i++)
		{
			if (!isBorder(i) && !border.equals(invItems[i]))
				items.add(invItems[i]);
		}

		return items;
	}

	private void saveItems()
	{
		System.out.println("saveItems");
	}

	private boolean isBorder(int i)
	{
		return i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8;
	}
}
