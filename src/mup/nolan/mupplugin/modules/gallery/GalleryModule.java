package mup.nolan.mupplugin.modules.gallery;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.db.GalleryRow;
import mup.nolan.mupplugin.db.GalleryUserdataRow;
import mup.nolan.mupplugin.db.MupDB;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.utils.ItemBuilder;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GalleryModule extends Module
{
	private final MupPlugin mupPlugin;
	private final Map<Player, GalleryView> viewMap = new HashMap<>();
	private final Set<Player> reminded = new HashSet<>();

	public GalleryModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "gallery");
		this.mupPlugin = mupPlugin;
	}

	@Override
	public void onDisable()
	{
		viewMap.keySet().forEach(Player::closeInventory);
		viewMap.clear();
		reminded.clear();
	}

	// todo move all to GuiManger
	public void open(OfflinePlayer owner, Player player, boolean editmode)
	{
		if (owner.getName() == null)
			return;

		// get all items and settings from db
		final Resrc<List<GalleryRow>> items = new Resrc<>(new ArrayList<>());
		final Resrc<GalleryUserdataRow> userdata = new Resrc<>();

		if (userdata == null) // set default userdata
			userdata = new GalleryUserdataRow(-1, owner, 0, "", cfg.getMaterial("gui-items.default-border"), null, null);

		// create inventory
		final String winName = cfg.getString("messages.gui.win-name." + (editmode ? "edit-" : "") + (owner == player ? "own" : "other"));
		final Inventory inv = Bukkit.createInventory(null, 54, StrUtils.replaceColors(winName.replace("{}", owner.getName())));

		// prepare items
		final ItemStack border = new ItemBuilder(userdata.getCurrentBorder()).withName("§").build();
		final ItemStack lockedSlot = new ItemBuilder(cfg.getMaterial("gui-items.locked-slot")).withName(cfg.getStringF("messages.gui.locked-slot")).build();
		final ItemStack prevPage = new ItemBuilder(cfg.getMaterial("gui-items.prev-page")).withName(cfg.getStringF("messages.gui.prev-page")).build();
		final ItemStack nextPage = new ItemBuilder(cfg.getMaterial("gui-items.next-page")).withName(cfg.getStringF("messages.gui.next-page")).build();
		final ItemStack borderMenu = new ItemBuilder(cfg.getMaterial("gui-items.border-buymenu")).withName(cfg.getStringF("messages.gui.border-buymenu")).build();
		final int borderMenuPos = cfg.getInt("gui-items.border-buymenu-pos");
		final ItemStack slotMenu = new ItemBuilder(cfg.getMaterial("gui-items.slot-buymenu")).withName(cfg.getStringF("messages.gui.slot-buymenu")).build();
		final int slotMenuPos = cfg.getInt("gui-items.slot-buymenu-pos");

		final List<String> infols = cfg.getStringList("messages.gui.info");
		infols.replaceAll(StrUtils::replaceColors);
		final ItemStack info = new ItemBuilder(cfg.getMaterial("gui-items.info")).withName(infols.remove(0)).withLore(infols).build();

		// set items (per page = 28)
		final Iterator<GalleryRow> it = items.iterator();
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
			else if (i == borderMenuPos)
				is = borderMenu;
			else if (i == slotMenuPos)
				is = slotMenu;

			else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
				is = border;
			else if (it.hasNext())
				is = it.next().getItem(); // if !editmode add date lockId

			inv.setItem(i, is);
		}

		player.openInventory(inv);
		viewMap.put(player, new GalleryView(items, userdata, inv, 0, editmode));

		if (editmode) // if opened editmode don't remind
			reminded.add(player);
	}

	public void onClick(InventoryClickEvent e)
	{
		final GalleryView view;
		if ((view = viewMap.get((Player)e.getWhoClicked())) != null)
			view.onClick(e);
	}

	public void onClose(InventoryCloseEvent e)
	{
		final GalleryView view;
		if ((view = viewMap.remove((Player)e.getPlayer())) == null)
			return;

		view.onClose(e);

		if (reminded.add((Player)e.getPlayer())) // exec once
			e.getPlayer().sendMessage(StrUtils.replaceColors(mupPlugin.getConfigManager().getConfig("gallery").getString("messages.edit-reminder")));
	}

	public void clearReminder(Player player)
	{
		reminded.remove(player);
	}
}