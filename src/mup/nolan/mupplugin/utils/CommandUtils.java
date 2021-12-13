package mup.nolan.mupplugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtils
{
	public static boolean playerOnlyCheck(CommandSender sender)
	{
		if (sender instanceof Player)
			return false;

		sender.sendMessage("§cTylko gracz może wykonać tą komende");
		return true;
	}

	public static int parseInt(String str)
	{
		return Integer.parseInt(str);
	}

	public static Player parsePlayer(String str)
	{
		return Bukkit.matchPlayer(str).isEmpty() ? null : Bukkit.matchPlayer(str).get(0);
	}
}
