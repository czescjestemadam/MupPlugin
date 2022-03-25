package mup.nolan.mupplugin.modules;

import com.Acrobot.ChestShop.Events.PreShopCreationEvent;
import mup.nolan.mupplugin.MupPlugin;

public class ChestshopFix extends Module
{
	public ChestshopFix(MupPlugin mupPlugin)
	{
		super(mupPlugin, "chestshop-fix");
	}

	public void onChestShop(PreShopCreationEvent e)
	{
		if (!this.isEnabled())
			return;

		final String sign = e.getSignLines()[2].replaceAll("[BS ]", "");
		final int maxPrice = cfg().getInt("max-price");
		if (Integer.parseInt(sign) > maxPrice)
		{
			e.setOutcome(PreShopCreationEvent.CreationOutcome.OTHER_BREAK);
			e.getPlayer().sendMessage(cfg().getStringF("messages.price-exceeded").replace("{}", String.valueOf(maxPrice)));
		}
	}
}
