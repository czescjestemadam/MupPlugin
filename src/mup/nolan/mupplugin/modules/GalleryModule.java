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
	private final Map<Player, List<GalleryRow>> itemsMap = new HashMap<>();
	private final Map<Player, GalleryUserdataRow> userdataMap = new HashMap<>();
	private final Map<Player, Inventory> invMap = new HashMap<>();
	private final Map<Player, Integer> pageMap = new HashMap<>();
	private final Map<Player, Boolean> editmodeMap = new HashMap<>();
	private final Set<Player> reminded = new HashSet<>();

	public GalleryModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "gallery");
		this.mupPlugin = mupPlugin;
	}

	@Override
	public void onDisable()
	{
		itemsMap.clear();
		userdataMap.clear();
		invMap.keySet().forEach(Player::closeInventory);
		invMap.clear();
		pageMap.clear();
		editmodeMap.clear();
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
			ResultSet rs = st.executeQuery("select * from mup_gallery where owner = '" + owner.getName() + "' " + (editmode ? "and lock_id is null" : "") + "order by sort_num");
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

		// add items and settings to map for player
		itemsMap.put(player, items);
		userdataMap.put(player, userdata);
		editmodeMap.put(player, editmode);
		pageMap.put(player, 0);

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
		final Iterator<GalleryRow> it = items.subList(0, 28).iterator();
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

		// open inv
		invMap.put(player, inv);
		player.openInventory(inv);

		if (editmode) // if opened editmode don't remind
			reminded.add(player);
	}

	public void onClick(InventoryClickEvent e)
	{
		if (!invMap.containsKey((Player)e.getWhoClicked())) // check if player have gallery opened
			return;

		// check for border settings click and render buy menu

		// check for page changes and rerender items sublist


		if (!editmodeMap.get((Player)e.getWhoClicked()))
			e.setCancelled(true);

		if (invMap.containsValue(e.getClickedInventory()))
		{
			if (e.getSlot() < 9 || e.getSlot() > 44 || e.getSlot() % 9 == 0 || e.getSlot() % 9 == 8)
				e.setCancelled(true);

			MupPlugin.log().info("clicked slot: " + e.getSlot());
		}
	}

	public void onClose(InventoryCloseEvent e)
	{
		final Player p = (Player)e.getPlayer();

		if (itemsMap.remove(p) == null)
			return;

		e.getView().setCursor(null);

		userdataMap.remove(p);
		invMap.remove(p);
		pageMap.remove(p);
		editmodeMap.remove(p);

		if (reminded.add((Player)e.getPlayer()))
			e.getPlayer().sendMessage(StrUtils.replaceColors(mupPlugin.getConfigManager().getConfig("gallery").getString("messages.edit-reminder")));

		Bukkit.getScheduler().scheduleSyncDelayedTask(mupPlugin, () -> ((Player)e.getPlayer()).updateInventory(), 1);
	}

	public void clearReminder(Player player)
	{
		reminded.remove(player);
	}
}
