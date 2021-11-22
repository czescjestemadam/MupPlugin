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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class GalleryView
{
	private static final Config cfg = MupPlugin.get().getConfigManager().getConfig("gallery");
	private static final int SLOTS_PER_PAGE = 28;

	private final Player player;
	private final OfflinePlayer owner;
	private final boolean editmode;
	private final Resrc<List<GalleryRow>> items = new Resrc<>();
	private final Resrc<GalleryUserdataRow> userdata = new Resrc<>();
	private final Inventory inv;

	// item and pos getters
	private final Supplier<ItemStack> border = () -> new ItemBuilder(userdata.get().getCurrentBorder()).withName("§").build();
	private final Supplier<ItemStack> lockedSlot = () -> new ItemBuilder(cfg.getMaterial("gui-items.locked-slot")).withName(cfg.getStringF("messages.gui.locked-slot")).build();
	private final Supplier<ItemBuilder> prevPage = () -> new ItemBuilder(cfg.getMaterial("gui-items.prev-page")).withName(cfg.getStringF("messages.gui.prev-page"));
	private final Supplier<ItemBuilder> nextPage = () -> new ItemBuilder(cfg.getMaterial("gui-items.next-page")).withName(cfg.getStringF("messages.gui.next-page"));
	private final Supplier<ItemStack> info = () -> {
		final List<String> infols = cfg.getStringList("messages.gui.info");
		infols.replaceAll(StrUtils::replaceColors);
		return new ItemBuilder(cfg.getMaterial("gui-items.info")).withName(infols.remove(0)).withLore(infols).build();
	};
	private final Supplier<ItemStack> borderMenu = () -> new ItemBuilder(cfg.getMaterial("gui-items.border-buymenu")).withName(cfg.getStringF("messages.gui.border-buymenu")).build();
	private final Supplier<Integer> borderMenuPos = () -> cfg.getInt("gui-items.border-buymenu-pos");
	private final Supplier<ItemStack> slotMenu = () -> new ItemBuilder(cfg.getMaterial("gui-items.slot-buymenu")).withName(cfg.getStringF("messages.gui.slot-buymenu")).withLore(cfg.getStringF("messages.gui.slot-buymenu-lore").replaceAll("\\{slot}", String.valueOf(userdata.get().getUnlockedSlots() + 1)).replaceAll("\\{price}", String.valueOf(getSlotPrice()))).build();
	private final Supplier<Integer> slotMenuPos = () -> cfg.getInt("gui-items.slot-buymenu-pos");
	private final Supplier<ItemStack> cancel = () -> new ItemBuilder(cfg.getMaterial("gui-items.cancel-submenu")).withName(cfg.getStringF("messages.gui.cancel-submenu")).build();
	private final Supplier<Integer> cancelPos = () -> cfg.getInt("gui-items.cancel-submenu-pos");
	private final Supplier<ItemStack> accept = () -> new ItemBuilder(cfg.getMaterial("gui-items.accept-submenu")).withName(cfg.getStringF("messages.gui.accept-submenu")).build();
	private final Supplier<Integer> acceptPos = () -> cfg.getInt("gui-items.accept-submenu-pos");

	private List<List<ItemStack>> invItems = new ArrayList<>();
	private GalleryViewType view = GalleryViewType.MAIN;
	private int page = 0;
	private int maxPages;

	public GalleryView(Player player, OfflinePlayer owner, boolean editmode)
	{
		this.player = player;
		this.owner = owner;
		this.editmode = editmode;

		readData();

		final String winName = cfg.getStringF("messages.gui.win-name." + (editmode ? "edit-" : "") + (owner == player ? "own" : "other"));
		inv = Bukkit.createInventory(null, 54, winName.replace("{}", owner.getName()));
		render(view, page);
		player.openInventory(inv);
	}

	public void onClick(InventoryClickEvent e)
	{
		final int i = e.getSlot();

		if (i == 48 && page > 0) // prev page
		{
			invItems.set(page, readInvItems());
			render(view, --page);
		}
		else if (i == 50 && page < maxPages - 1) // next page
		{
			invItems.set(page, readInvItems());
			render(view, ++page);
		}

		switch (view)
		{
			case MAIN -> {
				//

			}

			case BORDER -> {

			}
		}

		if (!editmode) // viewonly mode
			e.setCancelled(true);
		else if ((e.getClickedInventory() == inv && isBorder(i)) && view == GalleryViewType.MAIN || border.get().isSimilar(e.getCurrentItem()) || lockedSlot.get().isSimilar(e.getCurrentItem()))
			e.setCancelled(true);
	}

	public boolean onClose(InventoryCloseEvent e)
	{
		if (view.canClose())
		{
			if (editmode)
				saveData();
			return true;
		}

		cancel();
		Bukkit.getScheduler().scheduleSyncDelayedTask(MupPlugin.get(), () -> player.openInventory(inv), 1);
		return false;
	}

	private void render(GalleryViewType view, int page)
	{
		this.view = view;
		this.page = page;

		// prepare items
		final ItemStack border = this.border.get();
		final ItemStack lockedSlot = this.lockedSlot.get();
		final ItemStack nextPage = this.nextPage.get().withAmount(maxPages - page).build();
		final ItemStack prevPage = this.prevPage.get().withAmount(page + 1).build();
		final List<ItemStack> pageList = invItems.get(page);

		// set items in inventory
		for (int i = 0; i < inv.getSize(); i++)
		{
			ItemStack is = lockedSlot;

			if (i == 48)
				is = prevPage;
			else if (i == 49)
				is = info.get();
			else if (i == 50)
				is = nextPage;


			else if (i == borderMenuPos.get() && editmode && view == GalleryViewType.MAIN)
				is = borderMenu.get();
			else if (i == slotMenuPos.get() && editmode && view == GalleryViewType.MAIN)
				is = slotMenu.get();

			else if (i == cancelPos.get() && view.isSubmenu())
				is = cancel.get();
			else if (i == acceptPos.get() && view.isSubmenu())
				is = accept.get();

			else if (isBorder(i))
				is = border;

			else if (pageList.size() > getRelativePos(i))
				is = pageList.get(getRelativePos(i));

			inv.setItem(i, is);
		}
	}

	private void cancel()
	{


		render(view.getParent(), page);
	}

	private void accept()
	{


		render(view.getParent(), page);
	}

	private void readData()
	{
		final long start = System.currentTimeMillis();

		MupPlugin.get().getDB().getGalleryData(owner, editmode, items, userdata);
		maxPages = getSlots() / SLOTS_PER_PAGE + 1;

		MupPlugin.log().warning("readData editmode=" + editmode);

		// init invItems
		invItems = new ArrayList<>(maxPages);
		for (int i = 0; i < maxPages; i++)
		{
			final int arrSize = Math.min(SLOTS_PER_PAGE, getSlots() - i * SLOTS_PER_PAGE);
			final List<ItemStack> ls = new ArrayList<>(arrSize);
			for (int j = 0; j < arrSize; j++)
				ls.add(null);
			invItems.add(ls);
			MupPlugin.log().info("page %d has %d slots".formatted(i, arrSize));
		}

		final SimpleDateFormat sdf = new SimpleDateFormat(cfg.getString("messages.gui.item.added-fmt"));

		// add items to invItems
		for (GalleryRow row : items.get())
		{
			final int page = row.getSortNum() / SLOTS_PER_PAGE; // debug only
			final int place = row.getSortNum() % SLOTS_PER_PAGE; // debug only

			final ItemStack item;
			if (!editmode)
			{
				final List<String> lorels = cfg.getStringList("messages.gui.item.lore");
				lorels.replaceAll(StrUtils::replaceColors);
				lorels.replaceAll(s -> s.replaceAll("\\{added}", sdf.format(row.getPlaced())));
				lorels.replaceAll(s -> s.replaceAll("\\{lockId}", row.getLockId() == null ? cfg.getStringF("messages.gui.item.null-lockid") : row.getLockId()));
				item = new ItemBuilder(row.getItem()).addLore(lorels).build();
			}
			else
				item = row.getItem();

			invItems.get(page).set(place, item);
			MupPlugin.log().info("placed %s at %d page %d place".formatted(item, page, place));
		}

		MupPlugin.log().info("userdata " + userdata.get());
		MupPlugin.log().info("readData in %dms".formatted(System.currentTimeMillis() - start));
	}

	private List<ItemStack> readInvItems()
	{
		final List<ItemStack> ret = new ArrayList<>();
		for (int i = 0; i < inv.getSize(); i++)
		{
			final ItemStack item = inv.getStorageContents()[i];
			if (!isBorder(i) && !lockedSlot.get().isSimilar(item))
				ret.add(item);
		}
		MupPlugin.log().info("readInvItems ret = " + ret);
		return ret;
	}

	private void saveData()
	{
		final long start = System.currentTimeMillis();

		MupPlugin.log().warning("saveData");

		invItems.set(page, readInvItems());

		final List<Integer> updatedIdxs = new ArrayList<>();

		final List<GalleryRow> changed = new ArrayList<>();
		final List<GalleryRow> added = new ArrayList<>();
		final List<GalleryRow> removed = new ArrayList<>();

		for (int page = 0; page < invItems.size(); page++) // search for moved place
		{
			final List<ItemStack> pagels = invItems.get(page);

			for (int i = 0; i < pagels.size(); i++)
			{
				final int sortNum = page * SLOTS_PER_PAGE + i;
				final ItemStack item = pagels.get(i);
				final GalleryRow row = items.get().stream().filter(gi -> gi.getSortNum() == sortNum).findFirst().orElse(null);

				if (item == null && row == null) // noting changed
					continue;
				else if (item == null) // row != null: item removed?
				{
					MupPlugin.log().info("item removed");
					removed.add(row);
				}
				else if (row == null) // item != null: item added?
				{
					MupPlugin.log().info("item added");
					added.add(new GalleryRow(-1, owner, sortNum, item, null, null));
				}
				else if (!item.equals(row.getItem())) // both != null
				{
					MupPlugin.log().info("both != null and !equal");
				}

				MupPlugin.log().info("sortnum %d: for %s found %s".formatted(sortNum, item, row));
			}
		}
		MupPlugin.log().info("changed " + changed);
		MupPlugin.log().info("added " + added);
		MupPlugin.log().info("removed " + removed);

		final MupDB db = MupPlugin.get().getDB();
		db.updateGalleryData(changed);
		db.insertGalleryData(added);
		db.deleteGalleryData(removed);

		MupPlugin.log().info("saveData in %dms".formatted(System.currentTimeMillis() - start));
	}

	private boolean isInPlace(ItemStack item, int sortNum)
	{
		final GalleryRow row = items.get().stream().filter(i -> i.getSortNum() == sortNum).findFirst().orElse(null);
		if (row == null)
			return false;
		return row.getItem().equals(item);
	}

	private boolean isBorder(int i)
	{
		return i < 9 || i > inv.getSize() - 10 || i % 9 == 0 || i % 9 == 8;
	}

	private int getSlots()
	{
		return cfg.getInt("unlocked-slots." + VaultHook.getPerms().getPrimaryGroup(null, owner).toLowerCase(), cfg.getInt("unlocked-slots.default")) + userdata.get().getUnlockedSlots();
	}

	private int getRelativePos(int i)
	{
		return isBorder(i) ? -1 : i - 8 - i / 9 * 2;
	}

	private int getSlotPrice()
	{
		return cfg.getInt("slot-buy-multiplier") * (userdata.get().getUnlockedSlots() + 1);
	}
}
