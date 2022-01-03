package mup.nolan.mupplugin.modules.discord.commands;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

import java.util.List;

public abstract class DCommand
{
	private final String name;
	private final String mcPermission;
	private final List<String> aliases;

	public DCommand(String name, String mcPermission, String... aliases)
	{
		this.name = name;
		this.mcPermission = mcPermission;
		this.aliases = List.of(aliases);
	}

	public abstract void execute(Message og, MessageAuthor sender, List<String> args);

	public String getName()
	{
		return name;
	}

	public String getMcPermission()
	{
		return mcPermission;
	}

	public List<String> getAliases()
	{
		return aliases;
	}
}
