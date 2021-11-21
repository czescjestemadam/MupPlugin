package mup.nolan.mupplugin.modules.gallery;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.db.GalleryRow;
import mup.nolan.mupplugin.db.GalleryUserdataRow;
import mup.nolan.mupplugin.db.MupDB;
import mup.nolan.mupplugin.hooks.VaultHook;
import mup.nolan.mupplugin.utils.ItemBuilder;
import mup.nolan.mupplugin.utils.Resrc;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.util.*;

public class GalleryView
{
	private static final Config cfg = MupPlugin.get().getConfigManager().getConfig("gallery");

	private final Player player;
	private final OfflinePlayer owner;
	private final boolean editmode;
	private final Inventory inv;
	private final int borderMenuPos = cfg.getInt("gui-items.border-buymenu-pos");
	private final int slotMenuPos = cfg.getInt("gui-items.slot-buymenu-pos");
	private final int cancelPos = cfg.getInt("gui-items.cancel-submenu-pos");
	private final int acceptPos = cfg.getInt("gui-items.accept-submenu-pos");
	private final ItemStack lockedSlot = new ItemBuilder(cfg.getMaterial("gui-items.locked-slot")).withName(cfg.getStringF("messages.gui.locked-slot")).build();
	private final ItemStack cancel;
	private final ItemStack accept;
	private final List<Material> availableBorders = cfg.getMaterialList("gui-items.available-borders");
	private List<GalleryRow> items;
	private GalleryUserdataRow userdata;
	private Material selectedBorder;
	private Material borderBuySelected;
	private GalleryViewType type = GalleryViewType.MAIN;
	private int page = 0;
	private int allPages;

	public GalleryView(Player player, OfflinePlayer owner, boolean editmode)
	{
		this.player = player;
		this.owner = owner;
		this.editmode = editmode;
		cancel = editmode ? new ItemBuilder(cfg.getMaterial("gui-items.cancel-submenu")).withName(cfg.getStringF("messages.gui.cancel-submenu")).build() : null;
		accept = editmode ? new ItemBuilder(cfg.getMaterial("gui-items.accpet-submenu")).withName(cfg.getStringF("messages.gui.accept-submenu")).build() : null;

		final String winName = cfg.getString("messages.gui.win-name." + (editmode ? "edit-" : "") + (owner == player ? "own" : "other"));
		inv = Bukkit.createInventory(null, 54, StrUtils.replaceColors(winName.replace("{}", owner.getName())));
		renderPage(page, type);

		player.openInventory(inv);
	}

	public void onClick(InventoryClickEvent e)
	{
		final int i = e.getSlot();

		if (i == 48 && page > 0)
		{
			if (editmode && type == GalleryViewType.MAIN)
				saveItems();
			renderPage(--page, type);
		}
		else if (i == 50 && page < allPages)
		{
			if (editmode && type == GalleryViewType.MAIN)
				saveItems();
			renderPage(++page, type);
		}

		switch (type)
		{
			case MAIN -> {
				if (i == borderMenuPos && editmode)
				{
					saveItems();
					renderPage(page, GalleryViewType.BORDER);
				}
				else if (i == slotMenuPos && editmode)
				{
					saveItems();
					renderPage(page, GalleryViewType.SLOT);
				}

				if (!editmode) // if !editmode cancel item move
					e.setCancelled(true);
				else if (e.getClickedInventory() == inv && (isBorder(i) || lockedSlot.isSimilar(e.getCurrentItem())))
					e.setCancelled(true);
			}

			case BORDER -> {
				if (cancel.isSimilar(e.getCurrentItem()))
				{
					selectedBorder = userdata.getCurrentBorder();
					renderPage(page, GalleryViewType.MAIN);
				}
				else if (accept.isSimilar(e.getCurrentItem()))
				{
					userdata.setCurrentBorder(selectedBorder);
					renderPage(page, GalleryViewType.MAIN);
				}
				else if (!isBorder(i) && e.getCurrentItem() != null && availableBorders.contains(e.getCurrentItem().getType()))
				{
					if (userdata.getUnlockedBorders().contains(e.getCurrentItem().getType().name()) || e.getCurrentItem().getType() == cfg.getMaterial("gui-items.default-border"))
						selectedBorder = e.getCurrentItem().getType();
					else
					{
						borderBuySelected = e.getCurrentItem().getType();
						renderPage(page, GalleryViewType.BORDER_BUY);
					}
				}

				e.setCancelled(true);
			}

			case BORDER_BUY -> {

			}

			case SLOT -> {
				if (cancel.isSimilar(e.getCurrentItem()))
					renderPage(page, GalleryViewType.MAIN);
				else if (accept.isSimilar(e.getCurrentItem()))
				{

				}

				e.setCancelled(true);
			}
		}
	}

	public boolean onClose(InventoryCloseEvent e)
	{
		if (type == GalleryViewType.BORDER || type == GalleryViewType.SLOT) // prevent closing and open previous
		{
			selectedBorder = userdata.getCurrentBorder();
			renderPage(page, GalleryViewType.MAIN);
			Bukkit.getScheduler().scheduleSyncDelayedTask(MupPlugin.get(), () -> player.openInventory(inv), 1);
			return false;
		}
		else if (type == GalleryViewType.BORDER_BUY) // prevent closing and open previous
		{
			renderPage(page, GalleryViewType.BORDER);
			Bukkit.getScheduler().scheduleSyncDelayedTask(MupPlugin.get(), () -> player.openInventory(inv), 1);
			return false;
		}

		if (editmode && type == GalleryViewType.MAIN)
			saveItems();

		e.getView().setCursor(null);
		Bukkit.getScheduler().scheduleSyncDelayedTask(MupPlugin.get(), player::updateInventory, 1);
		return true;
	}

	private void renderPage(int page, GalleryViewType type)
	{
		this.type = type;

		if (type == GalleryViewType.MAIN)
		{
			final Resrc<List<GalleryRow>> items = new Resrc<>(new ArrayList<>());
			final Resrc<GalleryUserdataRow> userdata = new Resrc<>();
			MupPlugin.get().getDB().getGalleryData(owner, editmode, items, userdata);
			this.items = items.get();
			this.userdata = userdata.get() == null ? new GalleryUserdataRow(-1, owner, 0, "", cfg.getMaterial("gui-items.default-border"), null, null) : userdata.get();
			selectedBorder = this.userdata.getCurrentBorder();
			allPages = getAllSlots() / 28;
		}

		// prepare items
		final ItemStack border = new ItemBuilder(userdata.getCurrentBorder()).withName("§").build();
		final ItemStack prevPage = new ItemBuilder(cfg.getMaterial("gui-items.prev-page")).withName(cfg.getStringF("messages.gui.prev-page")).withAmount(page + 1).build();
		final ItemStack nextPage = new ItemBuilder(cfg.getMaterial("gui-items.next-page")).withName(cfg.getStringF("messages.gui.next-page")).withAmount(allPages - page + 1).build();
		final ItemStack borderMenu = editmode ? new ItemBuilder(cfg.getMaterial("gui-items.border-buymenu")).withName(cfg.getStringF("messages.gui.border-buymenu")).build() : null;
		final ItemStack slotMenu = editmode ? new ItemBuilder(cfg.getMaterial("gui-items.slot-buymenu")).withName(cfg.getStringF("messages.gui.slot-buymenu")).build() : null;

		final List<String> infols = cfg.getStringList("messages.gui.info");
		infols.replaceAll(StrUtils::replaceColors);
		final ItemStack info = new ItemBuilder(cfg.getMaterial("gui-items.info")).withName(infols.remove(0)).withLore(infols).build();

		// set items (per page = 28)
		List<GalleryRow> items = new ArrayList<>();
		if (type == GalleryViewType.MAIN)
			items = this.items;
		else if (type == GalleryViewType.BORDER)
			items = getBorderList();
		final List<GalleryRow> subItems = items.subList(Math.min(items.size(), page * 28), Math.min(items.size(), page * 28 + 27));
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
			else if (i == borderMenuPos && type == GalleryViewType.MAIN && editmode)
				is = borderMenu;
			else if (i == slotMenuPos && type == GalleryViewType.MAIN && editmode)
				is = slotMenu;

			else if (i == cancelPos && type != GalleryViewType.MAIN)
				is = cancel;
			else if (i == acceptPos && type != GalleryViewType.MAIN)
				is = accept;

			else if (isBorder(i))
				is = border;
			else if (subItems.size() > getRelativePos(i))
				is = subItems.get(getRelativePos(i)).getItem(); // if !editmode add date lockId
			else if (getRelativePos(i) + page * 28 < getAllSlots() && type == GalleryViewType.MAIN)
				is = null;

			inv.setItem(i, is);
		}
	}

	private List<GalleryRow> getBorderList()
	{
		final List<Material> unlocked = StrUtils.getMaterials(Arrays.asList(userdata.getUnlockedBorders().split(";")));
		final List<GalleryRow> ret = new ArrayList<>();
		availableBorders.forEach(m -> {
			final boolean isUnlocked = unlocked.contains(m) || cfg.getMaterial("gui-items.default-border") == m;
			final ItemBuilder item = new ItemBuilder(m).withName((isUnlocked ? "§a" : "§c") + StrUtils.capitalize(m.name().replaceAll("_", " ")));
			if (!isUnlocked)
				item.withLore(cfg.getStringF("messages.gui.border-buymenu-buylore").replace("{}", String.valueOf(getBorderCost(m))));
			if (userdata.getCurrentBorder() == m)
				item.addEnchantGlint().withLore(cfg.getStringF("messages.gui.border-buymenu-selected"));
			ret.add(GalleryRow.justItem(item.build()));
		});
		return ret;
	}

	private List<ItemStack> readInvContents(List<ItemStack> items)
	{
		final ItemStack[] invItems = inv.getStorageContents();
		final ItemStack border = new ItemBuilder(userdata.getCurrentBorder()).withName("§").build();

		for (int i = 0; i < inv.getSize(); i++)
		{
			if (!isBorder(i) && !border.isSimilar(invItems[i]) && !lockedSlot.isSimilar(invItems[i]))
				items.add(invItems[i]);
		}

		return items;
	}

	private void saveItems()
	{
		final List<ItemStack> invItems = readInvContents(new ArrayList<>());

		final List<GalleryRow> changed = new ArrayList<>();
		final List<GalleryRow> added = new ArrayList<>();
		final List<GalleryRow> removed = new ArrayList<>();

		for (ItemStack invItem : invItems) // every read item
		{
			int index;
			if ((index = contains(invItem)) >= 0) // read item is in db
			{
				if (index - page * 28 != invItems.indexOf(invItem)) // changed index (place)
				{
					items.get(index).setSortNum(invItems.indexOf(invItem) + page * 28); // update sort_num
					changed.add(items.get(index)); // db update sort_num
				}
			}
			else if (invItem != null) // item is not in db
				added.add(new GalleryRow(-1, owner, invItems.indexOf(invItem) + page * 28, invItem, new Date(), null)); // add item
		}

		// every db item
		for (GalleryRow gItem : items.subList(Math.min(items.size(), page * 28), Math.min(items.size(), page * 28 + 27)))
		{
			if (!containsDB(invItems, gItem.getItem())) // db item isn't in read items
				removed.add(gItem);
		}

		MupDB db = MupPlugin.get().getDB();
		db.updateGalleryData(changed);
		db.insertGalleryData(added);
		db.deleteGalleryData(removed);

		MupPlugin.log().warning("inv save");
		MupPlugin.log().info("invItems = " + invItems);
		MupPlugin.log().info("changed = " + changed);
		MupPlugin.log().info("added = " + added);
		MupPlugin.log().info("removed = " + removed);
	}

	private int contains(ItemStack is)
	{
		for (GalleryRow item : items)
		{
			if (Objects.deepEquals(is, item.getItem()))
				return items.indexOf(item);
		}
		return -1;
	}

	private boolean containsDB(List<ItemStack> ls, ItemStack item)
	{
		for (ItemStack i : ls)
		{
//			if (i == item || (i != null && i.equals(item)))
			if (Objects.equals(i, item))
				return true;
		}
		return false;
	}

	private boolean isBorder(int i)
	{
		return i < 9 || i > inv.getSize() - 10 || i % 9 == 0 || i % 9 == 8;
	}

	private int getRelativePos(int i)
	{
		if (!isBorder(i))
			return i - 8 - i / 9 * 2;
		return -1;
	}

	private int getAllSlots()
	{
		return cfg.getInt("unlocked-slots." + VaultHook.getPerms().getPrimaryGroup(null, owner).toLowerCase(), cfg.getInt("unlocked-slots.default")) + userdata.getUnlockedSlots();
	}

	private int getBorderCost(Material m)
	{
		return cfg.getInt("border-cost." + m.name(), cfg.getInt("border-cost.default"));
	}
}
