package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.config.ConfigManager;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
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

		if (args[0].equalsIgnoreCase("help"))
		{
			sender.sendMessage(
					"§8» §a" + mp.getDescription().getFullName(),
					"§9/{} module <enable|disable|reload> <module>".replace("{}", alias),
					"§9/{} reloadconfig <config|-a>".replace("{}", alias)
			);
		}
		else if (args[0].equalsIgnoreCase("module"))
		{
			if (args.length == 1)
			{
				sender.sendMessage(
						"§8» §aModules (§f" + mp.getModuleManager().getModules(true).size() + "§7/" + mp.getModuleManager().getModules(false).size() + "§a)",
						mp.getModuleManager().getModules(false).stream().map(m -> (mp.getModuleManager().isModuleEnabled(m) ? "§a" : "§c") + m).collect(Collectors.joining("§9, ", "§9[", "§9]"))
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
				sender.sendMessage(mp.getConfigManager().getConfig("modules").getStringF(cfgStr).replace("{}", mod.getName()));
			}
		}
		else if (args[0].equalsIgnoreCase("reloadconfig"))
		{
			final ConfigManager cm = mp.getConfigManager();
			if (args.length == 1)
			{
				sender.sendMessage("§9Loaded configs: §a" + cm.getConfigs().stream().collect(Collectors.joining("§9,§a ", "§9[§a", "§9]")));
				return true;
			}

			if (args[1].equalsIgnoreCase("-a"))
			{
				Bukkit.getScheduler().runTaskAsynchronously(mp, () -> {
					final long start = System.currentTimeMillis();
					cm.loadConfigs();
					final String msg = "§eReloaded all configs in {}ms async".replace("{}", String.valueOf(System.currentTimeMillis() - start));
					sender.sendMessage(msg);
					if (sender != Bukkit.getConsoleSender())
						MupPlugin.log().info(msg);
				});
			}
			else
			{
				final Config config = cm.getConfig(args[1]);
				if (config == null)
					return true;

				final long start = System.currentTimeMillis();
				config.load();
				final String msg = "§eReloaded {config} config in {time}ms"
						.replace("{config}", args[1])
						.replace("{time}", String.valueOf(System.currentTimeMillis() - start));
				sender.sendMessage(msg);
				if (sender != Bukkit.getConsoleSender())
					MupPlugin.log().info(msg);
			}

		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		if (args.length == 1)
			return StrUtils.returnMatches(args[0], List.of("module", "help", "reloadconfig"));

		final ModuleManager mm = MupPlugin.get().getModuleManager();

		if (args[0].equalsIgnoreCase("module"))
		{
			if (args.length == 2)
				return StrUtils.returnMatches(args[1], List.of("enable", "disable", "reload"));

			final List<String> ret = new ArrayList<>();

			if (args[1].toLowerCase().startsWith("e"))
				ret.addAll(mm.getModules(false).stream().filter(m -> !mm.isModuleEnabled(m)).toList());

			if (args[1].toLowerCase().startsWith("d") || args[1].toLowerCase().startsWith("r"))
				ret.addAll(mm.getModules(true));

			return StrUtils.returnMatches(args[2], ret);
		}
		else if (args[0].equalsIgnoreCase("reloadconfig"))
		{
			final List<String> ret = new ArrayList<>(List.of("-a"));
			ret.addAll(MupPlugin.get().getConfigManager().getConfigs());
			return StrUtils.returnMatches(args[1], ret);
		}

		return List.of();
	}
}
