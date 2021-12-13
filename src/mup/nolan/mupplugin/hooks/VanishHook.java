package mup.nolan.mupplugin.hooks;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class VanishHook
{
	public static boolean isVanished(Player player)
	{
		return player.getMetadata("vanished").stream().anyMatch(MetadataValue::asBoolean);
	}
}
