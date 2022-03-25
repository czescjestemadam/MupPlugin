package mup.nolan.mupplugin.listeners;

import com.Acrobot.ChestShop.Events.PreShopCreationEvent;
import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.ChestshopFix;
import mup.nolan.mupplugin.modules.ModuleManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChestShopListener implements Listener
{
	private final MupPlugin mupPlugin;
	private final ModuleManager mm;

	public ChestShopListener(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
		this.mm = mupPlugin.getModuleManager();
	}

	@EventHandler
	private void onChestShop(PreShopCreationEvent e)
	{
		((ChestshopFix)mm.getModule("chestshop-fix")).onChestShop(e);
	}
}
