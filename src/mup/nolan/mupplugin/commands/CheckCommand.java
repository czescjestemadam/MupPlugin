package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.hooks.VanishHook;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.modules.antiafk.AntiafkModule;
import mup.nolan.mupplugin.utils.CommandUtils;
import mup.nolan.mupplugin.utils.PermsUtils;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;

public class CheckCommand implements TabExecutor
{
	private final Map<String, Function<OfflinePlayer, String>> getters = new HashMap<>();
	private final ModuleManager mm;
	private final Config cfg;

	public CheckCommand()
	{
		getters.put("-afk", this::checkAntiafk);
		getters.put("-sort", this::checkSort);
		getters.put("-pos", this::checkPos);
		getters.put("-v", this::checkV);
		getters.put("-conn", this::checkConn);

		mm = MupPlugin.get().getModuleManager();
		cfg = MupPlugin.get().getConfigManager().getConfig("commands");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if (!PermsUtils.hasCmd(sender, "check", true))
			return true;

		final Set<String> flags = new HashSet<>();
		final Set<OfflinePlayer> targets = new HashSet<>();

		for (String arg : args)
		{
			if (arg.startsWith("-"))
			{
				if (arg.equalsIgnoreCase("-all"))
					flags.addAll(getters.keySet());
				else if (arg.equalsIgnoreCase("-default"))
					flags.addAll(List.of(cfg.getString("check.default-flags").split(" +")));
				else
					flags.add(arg);
			}
			else
			{
				final OfflinePlayer parsedPlayer = CommandUtils.parsePlayer(arg);
				if (parsedPlayer == null)
				{
					final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(arg);
					if (offlinePlayer.hasPlayedBefore())
						targets.add(offlinePlayer);
				}
				else
					targets.add(parsedPlayer);
			}
		}

		if (targets.isEmpty())
		{
			if (!(sender instanceof Player))
			{
				sender.sendMessage("§cNie podano graczy");
				return true;
			}
			targets.add((OfflinePlayer)sender);
		}

		if (flags.isEmpty())
			flags.addAll(List.of(cfg.getString("check.default-flags").split(" +")));

		final List<String> response = new ArrayList<>();

		for (OfflinePlayer target : targets)
		{
			response.add("§8» §aChecking " + target.getName());

			for (String flag : flags)
			{
				final Function<OfflinePlayer, String> getter = getters.get(flag);
				if (getter == null)
				{
					response.add("§cInvalid flag " + flag);
					continue;
				}
				final String flagResp = getter.apply(target);
				if (flagResp != null)
					response.add("§9" + flag + ": §f" + flagResp);
			}
		}

		response.forEach(sender::sendMessage);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		final List<String> argLs = List.of(args);

		final List<String> ret = new ArrayList<>(CommandUtils.getPlayerList());
		ret.add("-all");
		ret.add("-default");
		if (!argLs.contains("-all"))
			ret.addAll(getters.keySet());
		ret.removeAll(argLs);
		if (argLs.contains("-default"))
			ret.removeAll(List.of(cfg.getString("check.default-flags").split(" +")));

		return StrUtils.returnMatches(args[args.length - 1], ret);
	}

	private String checkAntiafk(OfflinePlayer player)
	{
		if (player.isOnline())
			return ((AntiafkModule)mm.getModule("antiafk")).getLastMove(player.getPlayer()).toShortString();
		return null;
	}

	private String checkSort(OfflinePlayer player)
	{
		return String.valueOf(MupPlugin.get().getDB().itemsortEnabled(player));
	}

	private String checkPos(OfflinePlayer player)
	{
		if (!player.isOnline())
			return null;

		final Location loc = player.getPlayer().getLocation();
		return "x:%.2f y:%.2f z:%.2f @ %s".formatted(loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
	}

	private String checkV(OfflinePlayer player)
	{
		if (player.isOnline())
			return String.valueOf(VanishHook.isVanished(player.getPlayer()));
		return null;
	}

	private String checkConn(OfflinePlayer player)
	{
		if (!player.isOnline())
			return null;

		final Player p = player.getPlayer();
		return "%s %sms".formatted(p.getAddress(), p.getPing());
	}
}
