package mup.nolan.mupplugin.modules.discord.commands;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

import java.util.List;

public class DPingCommand extends DCommand
{
	public DPingCommand()
	{
		super("ping", null);
	}

	@Override
	public void execute(Message og, MessageAuthor sender, List<String> args)
	{
		og.reply("Gateway: " + og.getApi().getLatestGatewayLatency().toMillis() + "ms");
	}
}
