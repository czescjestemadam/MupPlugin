package mup.nolan.mupplugin.modules.gallery;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		viewMap.put(player, new GalleryView(player, owner, editmode));

		if (editmode) // if opened editmode don't remind
			reminded.add(player);
	}

	public void onClick(InventoryClickEvent e)
	{
		final GalleryView view;
		if ((view = viewMap.get((Player)e.getWhoClicked())) != null && e.getCurrentItem() != null)
			view.onClick(e);
	}

	public void onClose(InventoryCloseEvent e)
	{
		final Player p = (Player)e.getPlayer();

		final GalleryView view;
		if ((view = viewMap.get(p)) == null)
			return;

		if (view.onClose())
			viewMap.remove(p);

		if (reminded.add(p)) // exec once
			e.getPlayer().sendMessage(StrUtils.replaceColors(mupPlugin.getConfigManager().getConfig("gallery").getString("messages.edit-reminder")));
	}

	public void clearReminder(Player player)
	{
		reminded.remove(player);
	}
}