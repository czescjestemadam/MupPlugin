package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.db.MupDB;
import mup.nolan.mupplugin.utils.CommandUtils;
import mup.nolan.mupplugin.utils.PermsUtils;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public class SortowanieCommand implements TabExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if (CommandUtils.playerOnlyCheck(sender) || MupPlugin.get().getModuleManager().checkEnabled("itemsort", sender) || !PermsUtils.hasCmd(sender, "itemsort", true))
			return true;

		final MupDB db = MupPlugin.get().getDB();
		final Player p = (Player)sender;

		final boolean val;
		if (args.length > 0 && args[0].equalsIgnoreCase("on"))
			val = true;
		else if (args.length > 0 && args[0].equalsIgnoreCase("off"))
			val = false;
		else
			val = !db.itemsortEnabled(p);

		db.setItemsort(p, val);

		final Config msgs = MupPlugin.get().getConfigManager().getConfig("itemsort");
		final String strval = msgs.getString(val ? "messages.enabled" : "messages.disabled");
		p.sendMessage(StrUtils.replaceColors(msgs.getString("messages.changed").replace("{}", strval)));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		if (args.length == 1)
			return StrUtils.returnMatches(args[0], List.of("on", "off"));

		return List.of();
	}
}
