package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.modules.discord.DiscordModule;
import mup.nolan.mupplugin.utils.PermsUtils;
import mup.nolan.mupplugin.utils.StrUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DiscordCommand implements TabExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if (!PermsUtils.hasCmd(sender, "discord", true))
			return true;

		final Config cfg = MupPlugin.get().getConfigManager().getConfig("discord");
		if (args.length == 0)
		{
			final String link = cfg.getString("messages.invite.click-url");
			final TextComponent msg = new TextComponent(cfg.getStringF("messages.invite.format").replaceAll("\\{link}", link));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
			sender.spigot().sendMessage(msg);
			return true;
		}

		if (args[0].equalsIgnoreCase("polacz"))
		{
			if (!PermsUtils.hasCmd(sender, "discord.link", true))
				return true;

			if (!(sender instanceof Player))
			{
				sender.sendMessage("§cTylko gracze moga wykonac ta komende");
				return true;
			}

			if (args.length == 1)
			{
				sender.sendMessage(cfg.getStringF("messages.link.help").replace("{}", alias));
				return true;
			}

			final String strId = args[1];

			if (!strId.matches("^\\d{18}$"))
			{
				sender.sendMessage(cfg.getStringF("messages.link.failure"));
				return true;
			}

			final long dcId;
			try
			{
				dcId = Long.parseLong(strId);
			} catch (NumberFormatException e)
			{
				sender.sendMessage(cfg.getStringF("messages.link.failure"));
				return true;
			}

			((DiscordModule)MupPlugin.get().getModuleManager().getModule("discord")).requestLink(sender, (Player)sender, dcId);
		}
		else if (args[0].equalsIgnoreCase("zweryfikuj"))
		{
			if (!PermsUtils.hasCmd(sender, "discord.link", true))
				return true;

			if (!(sender instanceof Player))
			{
				sender.sendMessage("§cTylko gracze moga wykonac ta komende");
				return true;
			}

			if (args.length == 1)
			{
				sender.sendMessage(cfg.getStringF("messages.verify.help").replace("{}", alias));
				return true;
			}

			final String code = args[1];

			if (!code.matches("^[0-9a-zA-Z]{6,16}$"))
			{
				sender.sendMessage(cfg.getStringF("messages.verify.failure"));
				return true;
			}

			((DiscordModule)MupPlugin.get().getModuleManager().getModule("discord")).verify(sender, (Player)sender, code);
		}
		else if (args[0].equalsIgnoreCase("rozlacz"))
		{
			if (!PermsUtils.hasCmd(sender, "discord.unlink", true))
				return true;

			if (!(sender instanceof Player))
			{
				sender.sendMessage("§cTylko gracze moga wykonac ta komende");
				return true;
			}

			((DiscordModule)MupPlugin.get().getModuleManager().getModule("discord")).unlink(sender, (Player)sender);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		if (args.length == 1)
		{
			final List<String> ret = new ArrayList<>();
			if (sender.hasPermission("mup.cmd.discord.link"))
				ret.addAll(List.of("polacz", "zweryfikuj"));
			if (sender.hasPermission("mup.cmd.discord.unlink"))
				ret.add("rozlacz");
			return StrUtils.returnMatches(args[0], ret);
		}

		return List.of();
	}
}
