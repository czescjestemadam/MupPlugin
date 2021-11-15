package mup.nolan.mupplugin.modules;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.db.GalleryRow;
import mup.nolan.mupplugin.db.GalleryUserdataRow;
import mup.nolan.mupplugin.db.MupDB;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

		final Config cfg = mupPlugin.getConfigManager().getConfig("gallery");

		// get all items and settings from db
		final List<GalleryRow> items = new ArrayList<>();
		GalleryUserdataRow userdata = null;

		final Statement st = mupPlugin.getDB().getStatement();
		try
		{
			ResultSet rs = st.executeQuery("select * from mup_gallery where owner = '" + owner.getName() + "' " + (editmode ? "and lock_id is null" : "") + " order by sort_num");
			while (rs.next())
				items.add(new GalleryRow(rs.getInt("id"), owner, rs.getInt("sort_num"), ItemBuilder.fromString(rs.getString("item")), rs.getString("lock_id")));

			rs = st.executeQuery("select " + (editmode ? "*" : "unlocked_slots, current_border") + " from mup_gallery_userdata where player = '" + owner.getName() + "'");
			if (rs.next())
			{
				final int slots = rs.getInt("unlocked_slots");

				userdata = editmode ?
						new GalleryUserdataRow(rs.getInt("id"), owner, slots, rs.getString("unlocked_borders"), Material.getMaterial(rs.getString("current_border")), null, null) :
						new GalleryUserdataRow(-1, owner, slots, null, Material.getMaterial(rs.getString("current_border")), null, null);
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		MupDB.closeStatement(st);

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
		if (!viewMap.containsKey((Player)e.getWhoClicked())) // check if player have gallery opened
			return;

		final GalleryView view = viewMap.get((Player)e.getWhoClicked());

		if (!view.editmode) // if !editmode cancel item move
			e.setCancelled(true);
		else if (e.getSlot() < 9 || e.getSlot() > 44 || e.getSlot() % 9 == 0 || e.getSlot() % 9 == 8) // else cancel only border
			e.setCancelled(true);

		// check for border settings click and render buy menu

		// check for page changes and rerender items sublist

	}

	public void onClose(InventoryCloseEvent e)
	{
		final Player p = (Player)e.getPlayer();

		if (viewMap.remove(p) == null)
			return;

		e.getView().setCursor(null);

		if (reminded.add((Player)e.getPlayer()))
			e.getPlayer().sendMessage(StrUtils.replaceColors(mupPlugin.getConfigManager().getConfig("gallery").getString("messages.edit-reminder")));

		Bukkit.getScheduler().scheduleSyncDelayedTask(mupPlugin, () -> ((Player)e.getPlayer()).updateInventory(), 1);
	}

	public void clearReminder(Player player)
	{
		reminded.remove(player);
	}

	private static class GalleryView
	{
		final List<GalleryRow> items;
		final GalleryUserdataRow userdataRow;
		final Inventory inv;
		int page;
		final boolean editmode;

		GalleryView(List<GalleryRow> items, GalleryUserdataRow userdataRow, Inventory inv, int page, boolean editmode)
		{
			this.items = items;
			this.userdataRow = userdataRow;
			this.inv = inv;
			this.page = page;
			this.editmode = editmode;
		}
	}
}
