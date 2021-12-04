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
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class GalleryView
{
	private static final Config cfg = MupPlugin.get().getConfigManager().getConfig("gallery");
	private static final NamespacedKey itemSortNumTag = new NamespacedKey(MupPlugin.get(), "placed");
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
	private final Supplier<ItemStack> slotMenu = () -> new ItemBuilder(cfg.getMaterial("gui-items.slot-buymenu")).withName(cfg.getStringF("messages.gui.slot-buymenu")).withLore(cfg.getStringF("messages.gui.slot-buymenu-lore").replaceAll("\\{slot}", String.valueOf(getSlots() + 1)).replaceAll("\\{price}", String.valueOf(getSlotPrice()))).build();
	private final Supplier<Integer> slotMenuPos = () -> cfg.getInt("gui-items.slot-buymenu-pos");
	private final Supplier<ItemStack> cancel = () -> new ItemBuilder(cfg.getMaterial("gui-items.cancel-submenu")).withName(cfg.getStringF("messages.gui.cancel-submenu")).build();
	private final Supplier<Integer> cancelPos = () -> cfg.getInt("gui-items.cancel-submenu-pos");
	private final Supplier<ItemStack> accept = () -> new ItemBuilder(cfg.getMaterial("gui-items.accept-submenu")).withName(cfg.getStringF("messages.gui.accept-submenu")).build();
	private final Supplier<Integer> acceptPos = () -> cfg.getInt("gui-items.accept-submenu-pos");

	private List<List<ItemStack>> invItems = new ArrayList<>();
	private GalleryViewType view = GalleryViewType.MAIN;
	private int page = 0;
	private int maxPages;
	private Material selectedBorder;
	private Material buyBorder;

	public GalleryView(Player player, OfflinePlayer owner, boolean editmode)
	{
		this.player = player;
		this.owner = owner;
		this.editmode = editmode;

		readData();
		selectedBorder = userdata.get().getCurrentBorder();

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
				if (i == borderMenuPos.get())
					render(GalleryViewType.BORDER, 0);
				else if (i == slotMenuPos.get())
					render(GalleryViewType.SLOT, 0);

				if (e.getAction() == InventoryAction.PICKUP_HALF)
					Bukkit.getScheduler().scheduleSyncDelayedTask(MupPlugin.get(), () -> removeTag(e.getCursor()), 1);
			}

			case BORDER -> {
				if (e.getClickedInventory() == inv)
				{
					if (i == cancelPos.get())
						cancel();
					else if (i == acceptPos.get())
						accept();
					else if (e.getCurrentItem() != null && !isBorder(i) && !lockedSlot.get().equals(e.getCurrentItem()))
					{
						if (userdata.get().isBorderUnlocked(e.getCurrentItem().getType()))
						{
							selectedBorder = e.getCurrentItem().getType();
							render(view, page);
						}
						else
						{
							buyBorder = e.getCurrentItem().getType();
							render(GalleryViewType.BORDER_BUY, page);
						}
					}
				}
			}

			case BORDER_BUY, SLOT -> {
				if (i == cancelPos.get())
					cancel();
				else if (i == acceptPos.get())
					accept();
			}
		}

		if (view == GalleryViewType.MAIN)
		{
			if (!editmode) // viewonly mode
				e.setCancelled(true);
			else if ((e.getClickedInventory() == inv && isBorder(i)) ||
					border.get().isSimilar(e.getCurrentItem()) ||
					lockedSlot.get().isSimilar(e.getCurrentItem()) ||
					e.getAction().toString().startsWith("DROP"))
				e.setCancelled(true);
		}
		else
			e.setCancelled(true);
	}

	public boolean onClose()
	{
		if (view.canClose())
		{
			if (editmode)
			{
				saveData();
				for (ItemStack item : player.getInventory().getContents()) // remove tag from taken items
					removeTag(item);
			}
			return true;
		}

		// reopen parrent menu
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
		final List<ItemStack> pageList;

		if (view == GalleryViewType.BORDER)
			pageList = getBorders(page);
		else
			pageList = invItems.get(page);

		// set items in inventory
		for (int i = 0; i < inv.getSize(); i++)
		{
			ItemStack is = lockedSlot;

			// menu buttons
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

			else if (i == cancelPos.get() && view != GalleryViewType.MAIN)
				is = cancel.get();
			else if (i == acceptPos.get() && view != GalleryViewType.MAIN)
				is = accept.get();

			else if (isBorder(i))
				is = border;

			// content
			else if (pageList.size() > getRelativePos(i) && !view.isSubmenu())
				is = pageList.get(getRelativePos(i));
			else if (i == 22 && view == GalleryViewType.BORDER_BUY)
				is = getBorder(buyBorder);
			else if (i == 22 && view == GalleryViewType.SLOT)
				is = slotMenu.get();

			inv.setItem(i, is);
		}
	}

	private void cancel()
	{
		if (view == GalleryViewType.BORDER)
			selectedBorder = userdata.get().getCurrentBorder();
		else if (view == GalleryViewType.BORDER_BUY)
			buyBorder = null;

		render(view.getParent(), page);
	}

	private void accept()
	{
		if (view == GalleryViewType.BORDER)
			userdata.get().setCurrentBorder(selectedBorder);
		else if (view == GalleryViewType.BORDER_BUY)
			buyBorder();
		else if (view == GalleryViewType.SLOT)
			buySlot();

		render(view.getParent(), page);
	}

	private void buyBorder()
	{
		final Material m = buyBorder;
		buyBorder = null;

		if (userdata.get().isBorderUnlocked(m))
		{
			MupPlugin.log().warning("border %s already unlocked for %s".formatted(m, player));
			return;
		}

		if (!VaultHook.hasEco())
		{
			player.sendMessage("§cWystąpił chwilowy błąd #ecobrrr");
			return;
		}

		final int price = getBorderPrice(m);
		if (!VaultHook.getEco().has(player, price))
		{
			player.sendMessage(cfg.getStringF("messages.no-money"));
			return;
		}

		final EconomyResponse resp = VaultHook.getEco().withdrawPlayer(player, price);
		if (!resp.transactionSuccess())
		{
			MupPlugin.log().warning("oop %s bal %d buying %s for %d".formatted(player, (int)VaultHook.getEco().getBalance(player), m.name(), price));
			player.sendMessage(cfg.getStringF("messages.no-money"));
			return;
		}

		userdata.get().unlockBorder(m);
		player.sendMessage(
				cfg.getStringF("messages.bought")
						.replace("{item}", StrUtils.capitalize(m.name().replaceAll("_", " ")))
						.replace("{price}", String.valueOf(price))
		);

		MupPlugin.log().warning("kupiono border " + userdata);
	}

	private void buySlot()
	{
		if (!VaultHook.hasEco())
		{
			player.sendMessage("§cWystąpił chwilowy błąd #ecobrrr");
			return;
		}

		final int slot = getSlots() + 1;

		if (!VaultHook.getEco().has(player, getSlotPrice()))
		{
			player.sendMessage(cfg.getStringF("messages.no-money"));
			return;
		}

		final EconomyResponse resp = VaultHook.getEco().withdrawPlayer(player, getSlotPrice());
		if (!resp.transactionSuccess())
		{
			MupPlugin.log().warning("oop %s bal %d buying %d slot for %d".formatted(player, (int)VaultHook.getEco().getBalance(player), slot, getSlotPrice()));
			player.sendMessage(cfg.getStringF("messages.no-money"));
			return;
		}

		userdata.get().unlockSlot();
		player.sendMessage(
				cfg.getStringF("messages.bought")
						.replace("{item}", slot + " slot")
						.replace("{price}", String.valueOf(getSlotPrice()))
		);

		MupPlugin.log().warning("kupiono slot " + userdata);
	}

	private void readData()
	{
		final long start = System.currentTimeMillis();

		MupPlugin.get().getDB().getGalleryData(owner, editmode, items, userdata);
		maxPages = getSlots() / SLOTS_PER_PAGE + 1;

		// init invItems
		invItems = new ArrayList<>(maxPages);
		for (int i = 0; i < maxPages; i++)
		{
			final int arrSize = Math.min(SLOTS_PER_PAGE, getSlots() - i * SLOTS_PER_PAGE);
			final List<ItemStack> ls = new ArrayList<>(arrSize);
			for (int j = 0; j < arrSize; j++)
				ls.add(null);
			invItems.add(ls);
		}

		final SimpleDateFormat sdf = new SimpleDateFormat(cfg.getString("messages.gui.item.added-fmt"));

		// add items to invItems
		for (GalleryRow row : items.get())
		{
			final int page = row.getSortNum() / SLOTS_PER_PAGE; // debug only
			final int place = row.getSortNum() % SLOTS_PER_PAGE; // debug only

			final ItemBuilder baseitem = new ItemBuilder(row.getItem()).withAmount(row.getAmount());
			final ItemStack item;
			if (!editmode)
			{
				final List<String> lorels = cfg.getStringList("messages.gui.item.lore");
				lorels.replaceAll(StrUtils::replaceColors);
				lorels.replaceAll(s -> s.replaceAll("\\{added}", sdf.format(row.getPlaced())));
				lorels.replaceAll(s -> s.replaceAll("\\{lockId}", row.getLockId() == null ? cfg.getStringF("messages.gui.item.null-lockid") : row.getLockId()));
				item = baseitem.addLore(lorels).build();
			}
			else
				item = addTag(baseitem.build(), row.getPlaced().getTime());

			invItems.get(page).set(place, item);
		}

		final String logstr = "readData %d items in %dms editmode=%b".formatted(items.get().size(), System.currentTimeMillis() - start, editmode);
		MupPlugin.log().warning(logstr);
		player.sendMessage(logstr);
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
		return ret;
	}

	private void saveData()
	{
		final long start = System.currentTimeMillis();

		invItems.set(page, readInvItems());

		Bukkit.getScheduler().runTaskAsynchronously(MupPlugin.get(), () -> {
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
					final long itemPlaced = getTag(item);
					final GalleryRow row = items.get().stream().filter(r -> r.getSortNum() == sortNum).findFirst().orElse(null);

					if (item == null && row == null)
						continue;
					else if (item == null)
						removed.add(row);
					else if (row == null)
						added.add(new GalleryRow(owner, sortNum, item, itemPlaced < 0 ? null : new Date(itemPlaced)).withAmountUpdate());
					else
					{
						if (item.isSimilar(row.getItem()))
						{
							if (item.getAmount() == row.getAmount())
								continue;

							row.setAmount(item.getAmount());
							changed.add(row.withAmountUpdate());
							continue;
						}

						removed.add(row);
						added.add(new GalleryRow(owner, sortNum, item, new Date(itemPlaced)));
					}
				}
			}

			final MupDB db = MupPlugin.get().getDB();
			db.upsertGalleryUserdata(userdata.get());
			db.updateGalleryData(changed);
			db.insertGalleryData(added);
			db.deleteGalleryData(removed);

			final String logstr = "async saveData %d, %d, %d items in %dms".formatted(changed.size(), added.size(), removed.size(), System.currentTimeMillis() - start);
			MupPlugin.log().warning(logstr);
			player.sendMessage(logstr);
		});
	}

	private ItemStack addTag(ItemStack item, long placed)
	{
		final ItemMeta im = item.getItemMeta();
		im.getCustomTagContainer().setCustomTag(itemSortNumTag, ItemTagType.LONG, placed);
		item.setItemMeta(im);
		return item;
	}

	private long getTag(ItemStack item)
	{
		if (item == null)
			return -1;
		return Optional.ofNullable(item.getItemMeta().getCustomTagContainer().getCustomTag(itemSortNumTag, ItemTagType.LONG)).orElse(-1L);
	}

	private ItemStack removeTag(ItemStack item)
	{
		if (item == null)
			return null;
		final ItemMeta im = item.getItemMeta();
		im.getCustomTagContainer().removeCustomTag(itemSortNumTag);
		item.setItemMeta(im);
		return item;
	}

	private List<ItemStack> getBorders(int page)
	{
		return cfg.getMaterialList("gui-items.available-borders").stream().map(this::getBorder).toList();
	}

	private ItemStack getBorder(Material m)
	{
		final boolean current = userdata.get().getCurrentBorder() == m;
		final boolean unlocked = userdata.get().isBorderUnlocked(m) || current;
		final ItemBuilder item = new ItemBuilder(m).withName((unlocked ? "§a" : "§c") + StrUtils.capitalize(m.name().replaceAll("_", " ")));
		if (current)
			item.withLore(cfg.getStringF("messages.gui.border-buymenu-selected"));
		else if (!unlocked)
			item.withLore(cfg.getStringF("messages.gui.border-buymenu-buylore").replace("{}", String.valueOf(getBorderPrice(m))));
		if (selectedBorder == m)
			item.addEnchantGlint();
		return item.build();
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

	private int getBorderPrice(Material m)
	{
		return cfg.getInt("border-cost." + m.name(), cfg.getInt("border-cost.default"));
	}
}
