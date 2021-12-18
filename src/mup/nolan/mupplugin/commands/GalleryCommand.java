package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.gallery.GalleryModule;
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
import java.util.List;

public class GalleryCommand implements TabExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if (CommandUtils.playerOnlyCheck(sender) || MupPlugin.get().getModuleManager().checkEnabled("gallery", sender) || !PermsUtils.hasCmd(sender, "gallery", true))
			return true;

		final Player p = (Player)sender;

		// galeria
		// galeria nik
		// galeria -edytuj
		// galeria -edytuj nik - special perms

		final boolean editmode = args.length > 0 && args[0].startsWith("-e");
		OfflinePlayer owner = p;

		if (args.length > 0 && !editmode)
			owner = Bukkit.getOfflinePlayer(args[0]);
		if (args.length > 1 && editmode && PermsUtils.hasCmd(sender, "gallery.edit-others"))
			owner = Bukkit.getOfflinePlayer(args[1]);

		if (!owner.hasPlayedBefore())
		{
			p.sendMessage(MupPlugin.get().getConfigManager().getConfig("gallery").getStringF("messages.no-player-found"));
			return true;
		}

		final GalleryModule gm = (GalleryModule)MupPlugin.get().getModuleManager().getModule("gallery");
		gm.open(owner, p, editmode);

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		final List<String> ret = new ArrayList<>();

		if (args.length == 1)
		{
			ret.add("-edytuj");
			ret.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
			return StrUtils.returnMatches(args[0], ret);
		}

		if (args.length == 2 && args[0].startsWith("-e") && PermsUtils.hasCmd(sender, "gallery.edit-others"))
			return null;

		return ret;
	}
}
