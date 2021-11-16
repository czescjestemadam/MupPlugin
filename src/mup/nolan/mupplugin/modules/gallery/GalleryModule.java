package mup.nolan.mupplugin.modules.gallery;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.db.GalleryRow;
import mup.nolan.mupplugin.db.GalleryUserdataRow;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.utils.Resrc;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

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
		// get all items and settings from db
		final Resrc<List<GalleryRow>> items = new Resrc<>(new ArrayList<>());
		final Resrc<GalleryUserdataRow> userdata = new Resrc<>();

		mupPlugin.getDB().getGalleryData(owner, editmode, items, userdata);

		final GalleryView view = new GalleryView(player, owner, editmode, items.get(), userdata.get());

		viewMap.put(player, view);

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