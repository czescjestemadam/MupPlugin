package mup.nolan.mupplugin.modules.discord;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.db.DiscordLink;
import mup.nolan.mupplugin.hooks.VaultHook;
import mup.nolan.mupplugin.modules.discord.commands.DCommand;
import mup.nolan.mupplugin.modules.discord.commands.DPingCommand;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class DiscordListener implements MessageCreateListener
{
	private final List<DCommand> commands = new ArrayList<>();
	private final DiscordBot bot;
	private final Config cfg;

	public DiscordListener(DiscordBot bot, Config cfg)
	{
		this.bot = bot;
		this.cfg = cfg;

		registerCommand(new DPingCommand());
	}

	@Override
	public void onMessageCreate(MessageCreateEvent e)
	{
		if (e.getMessageContent().startsWith(cfg.getString("bot.prefix")))
			handleCommand(e);
		else
			handleMessage(e);
	}

	private void handleCommand(MessageCreateEvent e)
	{
		final List<String> args = new ArrayList<>(List.of(e.getMessageContent().substring(1).trim().split(" +")));
		final String cmd = args.remove(0);

		final List<DCommand> commandLs = commands.stream().filter(c -> c.getName().equalsIgnoreCase(cmd) || c.getAliases().contains(cmd)).toList();
		if (!commandLs.isEmpty())
			commandLs.get(0).execute(e.getMessage(), e.getMessageAuthor(), args);
	}

	private void handleMessage(MessageCreateEvent e)
	{
		if (!e.getMessageAuthor().isRegularUser())
			return;

		if (cfg.getBool("commands.enabled"))
			handleMinecraftCommand(e);

		if (cfg.getBool("chat.from-discord.enabled"))
			handleMinecraftChat(e);
	}

	private void handleMinecraftCommand(MessageCreateEvent e)
	{
		final String channelName = cfg.getString("commands.channel");
		if (channelName == null || channelName.isEmpty())
			return;

		final String channelId = cfg.getString("channels." + channelName);
		if (channelId == null || channelId.isEmpty())
			return;

		if (!e.getChannel().getIdAsString().equals(channelId))
			return;

		final DiscordLink link = MupPlugin.get().getDB().getLinked(e.getMessageAuthor().getId());
		if (link == null || !link.isVerified())
		{
			e.getMessage().reply(cfg.getString("messages.not-linked"));
			return;
		}

		final CommandSender sender = new DiscordCommandSender(link, e.getChannel());
		final String cmd = e.getMessageContent().trim();
		Bukkit.getScheduler().runTask(MupPlugin.get(), () -> {
			try
			{
				Bukkit.dispatchCommand(sender, cmd);
			} catch (CommandException ex)
			{
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				final PrintStream stream = new PrintStream(os);
				ex.printStackTrace(stream);
				e.getChannel().sendMessage(os.toString());
			}
		});
	}

	private void handleMinecraftChat(MessageCreateEvent e)
	{
		final String channelName = cfg.getString("chat.channel");
		if (channelName == null || channelName.isEmpty())
			return;

		final String channelId = cfg.getString("channels." + channelName);
		if (channelId == null || channelId.isEmpty())
			return;

		if (!e.getChannel().getIdAsString().equals(channelId))
			return;

		final DiscordLink link = MupPlugin.get().getDB().getLinked(e.getMessageAuthor().getId());
		if (link == null || !link.isVerified())
		{
			e.getMessage().reply(cfg.getString("messages.not-linked"));
			return;
		}

		final Player p = link.getPlayer().getPlayer();
		if (p != null)
			Bukkit.getScheduler().runTask(MupPlugin.get(), () -> p.chat(e.getMessageContent()));
		else
		{
			final String prefix = VaultHook.getChat().getPlayerPrefix(null, link.getPlayer());
			final String suffix = VaultHook.getChat().getPlayerSuffix(null, link.getPlayer());
			final String msg = cfg.getStringF("chat.from-discord.format")
					.replace("{prefix}", prefix)
					.replace("{name}", StrUtils.safeNull(link.getPlayer().getName()))
					.replace("{suffix}", suffix)
					.replace("{msg}", e.getMessageContent());

			Bukkit.broadcastMessage(StrUtils.replaceColors(msg));
			e.getChannel().sendMessage(StrUtils.discordEscaped(StrUtils.removeColors(msg)));
		}
	}

	private void registerCommand(DCommand command)
	{
		if (commands.stream().noneMatch(c -> c.getName().equalsIgnoreCase(command.getName())))
			commands.add(command);
	}
}
