package mup.nolan.mupplugin.modules;

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

		
	}
}