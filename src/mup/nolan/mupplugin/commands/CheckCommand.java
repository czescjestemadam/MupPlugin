package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.utils.PermsUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckCommand implements TabExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if (!PermsUtils.hasCmd(sender, "check", true))
			return true;

		final ModuleManager mm = MupPlugin.get().getModuleManager();
		final List<String> response = new ArrayList<>();

		final List<String> flags = Arrays.stream(args).filter(str -> str.startsWith("-")).map(str -> str.substring(1)).toList();
		for (String flag : flags)
		{

		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		final List<String> ret = new ArrayList<>();



		return ret;
	}
}
