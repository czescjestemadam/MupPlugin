package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		return List.of();
	}
*/

public class MupCommand implements TabExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		final MupPlugin mp = MupPlugin.get();

		if (args.length == 0)
		{
			sender.sendMessage(
					"§8» §a" + mp.getDescription().getFullName(),
					"§9Loaded configs: §f" + mp.getConfigManager().getConfigs().size(),
					"§9Database: §f" + mp.getDB().getType(),
					"§9Enabled modules: §f" + mp.getModuleManager().getModules(true).size() + "§7/" + mp.getModuleManager().getModules(false).size(),
					"§9Registered listeners: §f" + mp.getListenerManager().getRegisteredNum(),
					"§9Registered commands: §f" + mp.getCommandManager().getCommandNum(),
					"§eMore at /" + alias + " help"
			);
			return true;
		}

		if (args[0].equalsIgnoreCase("module"))
		{
			if (args.length == 1)
			{
				sender.sendMessage(
						"§8» §aModules (§f" + mp.getModuleManager().getModules(true).size() + "§7/" + mp.getModuleManager().getModules(false).size() + "§a)",
						"§9[" + mp.getModuleManager().getModules(false).stream().map(m -> (mp.getModuleManager().isModuleEnabled(m) ? "§a" : "§c") + m).collect(Collectors.joining("§9, ")) + "§9]"
				);
				return true;
			}

			final String moduleUsage = "/{} module <enable | disable | reload> <module>";

			if (args.length != 3)
			{
				sender.sendMessage(moduleUsage.replace("{}", alias));
				return true;
			}

			final char action = args[1].toLowerCase().charAt(0);
			final Module mod = mp.getModuleManager().getModule(args[2]);

			if (mod == null)
			{
				sender.sendMessage("§cModule not found");
				return true;
			}

			switch (action)
			{
				case 'e' -> mod.setEnabled(true);
				case 'd' -> mod.setEnabled(false);
				case 'r' -> mod.reload();
				default -> {
					sender.sendMessage(moduleUsage.replace("{}", alias));
					return true;
				}
			}

			if (sender instanceof Player)
			{
				final String cfgStr = action == 'e' ? "messages.on-enable" : (action == 'd' ? "messages.on-disable" : "messages.on-reload");
				sender.sendMessage(StrUtils.replaceColors(mp.getConfigManager().getConfig("modules").getString(cfgStr).replace("{}", mod.getName())));
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		if (args.length == 1)
			return StrUtils.returnMatches(args[0], List.of("module", "help"));

		if (args[0].equalsIgnoreCase("module"))
		{
			if (args.length == 2)
				return StrUtils.returnMatches(args[1], List.of("enable", "disable", "reload"));

			final ModuleManager mm = MupPlugin.get().getModuleManager();

			final List<String> ret = new ArrayList<>();

			if (args[1].toLowerCase().startsWith("e"))
				ret.addAll(mm.getModules(false).stream().filter(m -> !mm.isModuleEnabled(m)).toList());

			if (args[1].toLowerCase().startsWith("d") || args[1].toLowerCase().startsWith("r"))
				ret.addAll(mm.getModules(true));

			return StrUtils.returnMatches(args[2], ret);
		}

		return List.of();
	}
}
