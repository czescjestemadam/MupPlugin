package mup.nolan.mupplugin.modules.discord;

import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.modules.discord.commands.DCommand;
import mup.nolan.mupplugin.modules.discord.commands.DPingCommand;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.core.entity.user.Member;

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
		if (e.getMessage().getMentionedUsers().contains(e.getApi().getYourself()))
			e.getMessage().reply("nub lol");

		if (!e.getMessageContent().startsWith(cfg.getString("bot.prefix")))
			return;

		final List<String> args = new ArrayList<>(List.of(e.getMessageContent().substring(1).trim().split(" +")));
		final String cmd = args.remove(0);

		final List<DCommand> commandLs = commands.stream().filter(c -> c.getName().equalsIgnoreCase(cmd) || c.getAliases().contains(cmd)).toList();
		commandLs.get(0).execute(e.getMessage(), e.getMessageAuthor(), args);
	}

	private void registerCommand(DCommand command)
	{
		if (commands.stream().noneMatch(c -> c.getName().equalsIgnoreCase(command.getName())))
			commands.add(command);
	}
}
