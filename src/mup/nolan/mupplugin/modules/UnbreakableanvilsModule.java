package mup.nolan.mupplugin.modules;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.hooks.WGHook;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class UnbreakableanvilsModule extends Module
{
	public UnbreakableanvilsModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "unbreakableanvils");
	}

	@Override
	public void onEnable()
	{
		if (!WGHook.hasFlag(WGHook.unbreakableAnvils))
		{
			MupPlugin.log().warning("No unbreakable-anvils flag");
			this.setEnabled(false);
		}
	}

	public void onAnvil(InventoryCloseEvent e)
	{
		if (!this.isEnabled())
			return;

		if (!WGHook.hasFlag(WGHook.unbreakableAnvils))
		{
			MupPlugin.log().warning("No unbreakable-anvils flag");
			this.setEnabled(false);
			return;
		}

		final Inventory inv = e.getInventory();

		if (inv.getType() != InventoryType.ANVIL)
			return;

		final Location loc = inv.getLocation();
		if (loc != null && WGHook.getFlagState(loc, (Player)e.getPlayer(), WGHook.unbreakableAnvils))
			fix(loc.getBlock());
	}

	private void fix(Block anvil)
	{
		Directional data = (Directional)anvil.getBlockData();
		final BlockFace facing = data.getFacing();

		if (anvil.getType() != Material.ANVIL)
			anvil.setType(Material.ANVIL);

		data = (Directional)anvil.getBlockData();
		data.setFacing(facing);
		anvil.setBlockData(data);
	}
}