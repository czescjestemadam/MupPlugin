package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.db.ReportsBlacklistRow;
import mup.nolan.mupplugin.db.ReportsRow;
import mup.nolan.mupplugin.modules.reports.ReportsModule;
import mup.nolan.mupplugin.utils.CommandUtils;
import mup.nolan.mupplugin.utils.PermsUtils;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ReportCommand implements TabExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if (!PermsUtils.hasCmd(sender, "report", true))
			return true;

		final Config cfg = MupPlugin.get().getConfigManager().getConfig("reports");
		if (args.length == 0)
		{
			sender.sendMessage(cfg.getStringF("messages.help").replace("{}", alias));
			return true;
		}

		final ReportsModule mod = (ReportsModule)MupPlugin.get().getModuleManager().getModule("reports");

		if (args[0].equalsIgnoreCase("-blacklist") && PermsUtils.hasCmd(sender, "report.blacklist"))
		{
			final String help = "§c/%s -blacklist <check|add|remove> <player> [time]".formatted(alias);

			if (args.length < 3)
			{
				sender.sendMessage(help);
				return true;
			}

			OfflinePlayer parsedPlayer = CommandUtils.parsePlayer(args[2]);
			if (parsedPlayer == null)
				parsedPlayer = Bukkit.getOfflinePlayer(args[2]);

			if (args[1].equalsIgnoreCase("check"))
			{
				final ReportsBlacklistRow blacklist = mod.checkBlacklist(parsedPlayer);
				sender.sendMessage("§c" + (blacklist == null ? "Player is not on blacklist" : blacklist.toString()));
			}
			else if (args[1].equalsIgnoreCase("add"))
			{
				final ReportsBlacklistRow blacklist = mod.checkBlacklist(parsedPlayer);
				if (blacklist != null)
				{
					sender.sendMessage("§cPlayer is on blacklist");
					return true;
				}

				final Date expires = args.length > 3 ? StrUtils.parseTimeDiff(args[3], new Date()) : null;
				mod.addBlacklist(parsedPlayer, expires);
				sender.sendMessage("§cAdded player %s to blacklist until %s".formatted(parsedPlayer.getName(), expires));
			}
			else if (args[1].equalsIgnoreCase("remove"))
			{
				final ReportsBlacklistRow blacklist = mod.checkBlacklist(parsedPlayer);
				if (blacklist == null)
				{
					sender.sendMessage("§cPlayer is not on blacklist");
					return true;
				}

				mod.removeBlacklist(blacklist);
				sender.sendMessage("§cRemoved %s from blacklist".formatted(parsedPlayer.getName()));
			}
			else
				sender.sendMessage(help);

			return true;
		}
		if (args[0].startsWith("-") && PermsUtils.hasCmd(sender, "report.manage"))
		{
			final String action = args[0];

			int id = -1;
			OfflinePlayer from = null;
			String type = null;
			OfflinePlayer player = null;
			boolean all = false;

			final List<String> flags = new ArrayList<>(List.of(Arrays.copyOfRange(args, 1, args.length)));
			if (flags.size() < 2)
			{
				sender.sendMessage("§cFlags: -from -type -player -all");
				return true;
			}

			for (int i = 0; i < flags.size(); i++)
			{
				final String val = flags.get(i);

				if (val.startsWith("#"))
				{
					id = Integer.parseInt(val.substring(1));
					break;
				}

				if (i < 1)
					continue;

				final String flag = flags.get(i - 1);

				if (flag.equalsIgnoreCase("-from"))
				{
					from = CommandUtils.parsePlayer(val);
					if (from == null)
						from = Bukkit.getOfflinePlayer(val);
				}
				else if (flag.equalsIgnoreCase("-type"))
				{
					type = val;
				}
				else if (flag.equalsIgnoreCase("-player"))
				{
					player = CommandUtils.parsePlayer(val);
					if (player == null)
						player = Bukkit.getOfflinePlayer(val);
				}

				if (val.equalsIgnoreCase("-all"))
					all = true;
			}

			final List<ReportsRow> rows = new ArrayList<>();

			if (id > 0)
				rows.add(mod.getReportById(id));
			else
				rows.addAll(mod.getReports(from, type, player, all));

			if (action.equalsIgnoreCase("-get"))
				mod.sendFormatedReport(sender, rows);
			else if (action.equalsIgnoreCase("-checked"))
				mod.checkedReports(rows);
			else if (action.equalsIgnoreCase("-remove"))
				mod.removeReports(rows);
		}

		if (!(sender instanceof final Player p))
			return true;

		final ReportsBlacklistRow blacklist = mod.checkBlacklist(p);
		if (blacklist != null && !blacklist.isExpired())
		{
			p.sendMessage(cfg.getStringF("messages.on-blacklist"));
			return true;
		}

		for (String type : cfg.list("types"))
		{
			if (!args[0].equalsIgnoreCase(type))
				continue;

			final List<String> required = cfg.getStringList("types." + type + ".require");

			if (required.size() >= args.length)
			{
				p.sendMessage(cfg.getStringF("types." + type + ".help").replace("{}", alias));
				return true;
			}

			required.removeIf(s -> s.equalsIgnoreCase("comment"));

			OfflinePlayer player = null;

			int i;
			for (i = 0; i < required.size(); i++)
			{
				if ("player".equals(required.get(i)))
				{
					player = CommandUtils.parsePlayer(args[1 + i]);
					if (player == null)
						player = Bukkit.getOfflinePlayer(args[1 + i]);
				}
			}

			mod.report(p, type, player, p.getLocation(), String.join(" ", Arrays.copyOfRange(args, 1 + required.size(), args.length)));
			return true;
		}

		sender.sendMessage(cfg.getStringF("messages.wrong-type").replace("{}", String.join(", ", cfg.list("types"))));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		final Config cfg = MupPlugin.get().getConfigManager().getConfig("reports");

		if (args.length == 1)
		{
			final List<String> ret = new ArrayList<>();

			if (sender instanceof Player)
				ret.addAll(cfg.list("types"));

			if (PermsUtils.hasCmd(sender, "report.blacklist"))
				ret.add("-blacklist");

			if (PermsUtils.hasCmd(sender, "report.manage"))
			{
				ret.add("-get");
				ret.add("-checked");
				ret.add("-remove");
			}

			return StrUtils.returnMatches(args[0], ret);
		}

		if (args[0].startsWith("-"))
		{
			if (args[0].equalsIgnoreCase("-blacklist") && PermsUtils.hasCmd(sender, "report.blacklist"))
			{
				if (args.length == 2)
					return StrUtils.returnMatches(args[1], List.of("check", "add", "remove"));
				if (args.length == 3)
					return null;
			}

			if (!PermsUtils.hasCmd(sender, "report.manage"))
				return List.of();

			final String lastArg = args[args.length - 2];
			if (lastArg.equalsIgnoreCase("-from") || lastArg.equalsIgnoreCase("-player"))
				return null;
		}
		else
		{
			if (!cfg.has("types." + args[0]))
				return List.of();

			final List<String> required = cfg.getStringList("types." + args[0] + ".require");
			if (required.isEmpty())
				return List.of();

			final int getIdx = args.length - 2;
			if (getIdx >= required.size())
				return List.of();

			final String reqStr = required.get(getIdx);
			if (reqStr.equalsIgnoreCase("player"))
				return null;
		}

		return List.of();
	}
}
