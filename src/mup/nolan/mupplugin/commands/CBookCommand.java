package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.modules.cbook.CBookModule;
import mup.nolan.mupplugin.modules.cbook.books.CBook;
import mup.nolan.mupplugin.utils.CommandUtils;
import mup.nolan.mupplugin.utils.PermsUtils;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public class CBookCommand implements TabExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		final ModuleManager mm = MupPlugin.get().getModuleManager();

		if (CommandUtils.playerOnlyCheck(sender) || mm.checkEnabled("cbook", sender) || !PermsUtils.hasCmd(sender, "cbook"))
			return true;

		final CBookModule mod = (CBookModule)mm.getModule("cbook");
		final Config cfg = MupPlugin.get().getConfigManager().getConfig("cbook");

		if (args.length == 0)
		{
			sender.sendMessage(cfg.getStringF("messages.usage").replace("{}", alias));
			return true;
		}

		final CBook book = mod.getBook(args[0]);
		if (book == null)
		{
			sender.sendMessage(cfg.getStringF("messages.not-found"));
			return true;
		}

		sender.sendMessage(cfg.getStringF("messages.opened").replace("{}", book.getName()));
		book.open((Player)sender);

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		final CBookModule mod = (CBookModule)MupPlugin.get().getModuleManager().getModule("cbook");
		return StrUtils.returnMatches(args[0], mod.getBooks());
	}
}
