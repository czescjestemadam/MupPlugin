package mup.nolan.mupplugin.modules.discord;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.db.DiscordLink;
import mup.nolan.mupplugin.hooks.VaultHook;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class DiscordModule extends Module
{
	private final DiscordBot bot;

	public DiscordModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "discord");
		bot = new DiscordBot(this, cfg());
	}

	@Override
	public void onEnable()
	{
		bot.start(() -> {
			if (cfg().getBool("on-off-message.enabled"))
				bot.sendMessage(cfg().getString("on-off-message.channel"), cfg().getString("messages.on-message"));
		});
	}

	@Override
	public void onDisable()
	{
		if (cfg().getBool("on-off-message.enabled"))
			bot.sendMessage(cfg().getString("on-off-message.channel"), cfg().getString("messages.off-message"));
		bot.stop();
	}

	public void onChat(AsyncPlayerChatEvent e)
	{
		if ((e.isCancelled() && !cfg().getBool("chat.send-cancelled")) || !this.isEnabled() || !cfg().getBool("chat.enabled"))
			return;

		final String primaryGroup = VaultHook.getPerms().getPrimaryGroup(e.getPlayer());
		final String player = e.getPlayer().getName();
		final String msg = e.getMessage();

		final String formatedMsg = cfg().getString("messages.chat-format")
				.replaceAll("\\{rank_e}", StrUtils.discordEscaped(primaryGroup))
				.replaceAll("\\{rank}", primaryGroup)
				.replaceAll("\\{player_e}", StrUtils.discordEscaped(player))
				.replaceAll("\\{player}", player)
				.replace("{msg_e}", StrUtils.discordEscaped(msg))
				.replace("{msg}", msg)
				.replaceAll("@here", "@\u200Bhere")
				.replaceAll("@everyone", "@\u200Beveryone");

		bot.sendMessage(cfg().getString("chat.channel"), formatedMsg);
	}

	public void requestLink(CommandSender sender, OfflinePlayer player, long discordId)
	{
		String code = null;
		for (int i = 0; i < cfg().getInt("link.max-code-generations"); i++)
		{
			final String tempCode = StrUtils.random(cfg().getInt("link.code-len"));
			if (!MupPlugin.get().getDB().linkCodeExists(tempCode))
			{
				code = tempCode;
				break;
			}
		}

		if (code == null)
		{
			sender.sendMessage(cfg().getStringF("messages.link.failure-codegen"));
			return;
		}

		final DiscordLink link = new DiscordLink(player, discordId, code, false);

		final boolean success = mup().getDB().requestLink(link);
		sender.sendMessage(cfg().getStringF(success ? "messages.link.success" : "messages.link.failure-linked"));
		if (success)
		{
			bot.sendVerificationMessage(link);

			final String channel = cfg().getString("link.discord.channel");
			final String msg = cfg().getString("link.discord.request-format")
					.replaceAll("\\{player}", player.getName())
					.replaceAll("\\{player_e}", StrUtils.discordEscaped(player.getName()))
					.replaceAll("\\{id}", String.valueOf(link.getDiscordId()));

			if (channel != null && !channel.isEmpty() && !msg.isEmpty())
				bot.sendMessage(channel, msg);
		}
	}

	public void verify(CommandSender sender, OfflinePlayer player, String code)
	{
		final DiscordLink link = mup().getDB().getLinked(player);
		if (link == null)
		{
			sender.sendMessage(cfg().getStringF("messages.verify.failure-linked"));
			return;
		}

		if (!link.getVerificationCode().equals(code))
		{
			sender.sendMessage(cfg().getStringF("messages.verify.failure"));
			return;
		}

		final boolean success = mup().getDB().verify(link);
		if (success)
		{
			sender.sendMessage(cfg().getStringF("messages.verify.success"));

			final String channel = cfg().getString("link.discord.channel");
			final String msg = cfg().getString("link.discord.link-format")
					.replaceAll("\\{player}", player.getName())
					.replaceAll("\\{player_e}", StrUtils.discordEscaped(player.getName()))
					.replaceAll("\\{id}", String.valueOf(link.getDiscordId()));

			if (channel != null && !channel.isEmpty() && !msg.isEmpty())
				bot.sendMessage(channel, msg);
		}
		else
			sender.sendMessage(cfg().getStringF("messages.verify.failure-verified"));
	}

	public void unlink(CommandSender sender, OfflinePlayer player)
	{
		final DiscordLink link = mup().getDB().getLinked(player);
		if (link == null)
		{
			sender.sendMessage(cfg().getStringF("messages.unlink.failure"));
			return;
		}

		mup().getDB().unlink(link);
		sender.sendMessage(cfg().getStringF("messages.unlink.success"));

		final String channel = cfg().getString("link.discord.channel");
		final String msg = cfg().getString("link.discord.unlink-format")
				.replaceAll("\\{player}", player.getName())
				.replaceAll("\\{player_e}", StrUtils.discordEscaped(player.getName()))
				.replaceAll("\\{id}", String.valueOf(link.getDiscordId()));

		if (channel != null && !channel.isEmpty() && !msg.isEmpty())
			bot.sendMessage(channel, msg);
	}

	public DiscordBot getBot()
	{
		return bot;
	}
}
