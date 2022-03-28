package mup.nolan.mupplugin.modules;

import mup.nolan.mupplugin.MupPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class UnbreakableanvilsModule extends Module
{
	public UnbreakableanvilsModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "unbreakableanvils");
	}

	public void onAnvil(InventoryCloseEvent e)
	{
		if (!this.isEnabled())
			return;

		final Inventory inv = e.getInventory();

		if (inv.getType() != InventoryType.ANVIL)
			return;

		final Location loc = inv.getLocation();
		if (loc != null)
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