package mup.nolan.mupplugin.modules;

import mup.nolan.mupplugin.MupPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class ButelkaModule extends Module
{
	public ButelkaModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "butelka");
	}

	public int cashouu(Player player, int i)
	{
		final int pExp = player.getTotalExperience();
		final int bottles = Math.min(Math.min(pExp / 10, ((int)Arrays.stream(player.getInventory().getStorageContents()).filter(Objects::isNull).count()) * 64), i);

		player.setExp(0);
		player.setLevel(0);
		player.setTotalExperience(0);
		player.giveExp(pExp - bottles * 10);
		player.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, bottles));

		return bottles;
	}
}
