package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.utils.CommandUtils;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public class FeedCommand implements TabExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if (!(sender instanceof Player) && args.length == 0)
		{
			sender.sendMessage("§c/" + alias + " <player> [amt]");
			return true;
		}

		final Player p = args.length > 0 ? CommandUtils.parsePlayer(args[0]) : (Player)sender;

		if (p == null)
		{
			sender.sendMessage("§cNie znaleziono gracza " + args[0]);
			return true;
		}

		final int lvl = args.length > 1 ? Integer.parseInt(args[1]) : 20;
		p.setFoodLevel(lvl);
		sender.sendMessage("§eNakarmiono " + p.getName() + " do " + lvl + " poziomu");

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		if (args.length == 1)
			return null;

		if (args.length == 2)
			return StrUtils.returnMatches(args[1], List.of("0", "1", "2", "5", "10", "15", "20"));

		return List.of();
	}
}
