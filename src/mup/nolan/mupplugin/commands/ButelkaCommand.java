package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.modules.BottlexpModule;
import mup.nolan.mupplugin.utils.PermsUtils;
import mup.nolan.mupplugin.utils.StrUtils;
import mup.nolan.mupplugin.utils.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ButelkaCommand implements TabExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if (CommandUtils.playerOnlyCheck(sender) || MupPlugin.get().getModuleManager().checkEnabled("bottlexp", sender) || !PermsUtils.hasCmd(sender, "bottlexp", true))
			return true;

		final Player p = ((Player)sender);
		final Config msgcfg = MupPlugin.get().getConfigManager().getConfig("bottlexp");

		if (args.length == 0)
		{
			p.sendMessage(StrUtils.replaceColors(msgcfg.getString("messages.usage")));
			return true;
		}

		final int i = args[0].equalsIgnoreCase("max") ? Integer.MAX_VALUE : CommandUtils.parseInt(args[0]);
		final int fi = ((BottlexpModule)MupPlugin.get().getModuleManager().getModule("bottlexp")).cashouu(p, i);

		if (fi > 0)
			p.sendMessage(StrUtils.replaceColors(msgcfg.getString("messages.given").replace("{}", String.valueOf(fi))));
		else
			p.sendMessage(StrUtils.replaceColors(msgcfg.getString("messages.not-enough")));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		final List<String> ret = new ArrayList<>();
		if (args.length == 1)
			StringUtil.copyPartialMatches(args[0], List.of("max", "16", "32", "64", "128", "256"), ret);
		return ret;
	}
}
