package mup.nolan.mupplugin.commands;

import mup.nolan.mupplugin.MupPlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

public class CommandManager
{
	private final MupPlugin mupPlugin;
	private int commandNum = 0;

	public CommandManager(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
	}

	public void registerCommands()
	{
		register(mupPlugin.getCommand("mupplugin"), new MupCommand());
		register(mupPlugin.getCommand("list"), new ListCommand());
		register(mupPlugin.getCommand("sortowanie"), new SortowanieCommand());
		register(mupPlugin.getCommand("butelka"), new ButelkaCommand());
		register(mupPlugin.getCommand("feed"), new FeedCommand());
		register(mupPlugin.getCommand("heal"), new HealCommand());
		register(mupPlugin.getCommand("galeria"), new GalleryCommand());
		register(mupPlugin.getCommand("check"), new CheckCommand());
	}

	private void register(PluginCommand pc, TabExecutor cmd)
	{
		if (pc == null)
			return;
		pc.setExecutor(cmd);
		pc.setTabCompleter(cmd);
		commandNum++;
	}

	public int getCommandNum()
	{
		return commandNum;
	}
}
