package mup.nolan.mupplugin.utils;

import mup.nolan.mupplugin.MupPlugin;
import org.bukkit.command.CommandSender;

public class PermsUtils
{
	public static boolean hasCmd(CommandSender sender, String perm)
	{
		return hasCmd(sender, perm, false);
	}

	public static boolean hasCmd(CommandSender sender, String perm, boolean verbose)
	{
		if (!sender.hasPermission("mup.cmd." + perm))
		{
			if (verbose)
				sender.sendMessage(StrUtils.replaceColors(MupPlugin.get().getConfigManager().getConfig("messages").getString("command.no-perms")));
			return false;
		}
		return true;
	}
}
