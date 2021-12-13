package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class ListenerManager
{
	private final MupPlugin mupPlugin;
	private int registeredNum = 0;

	public ListenerManager(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
	}

	public void registerListeners()
	{
		register(new ConnectionListener(mupPlugin));
		register(new InventoryListener(mupPlugin));
		register(new ExpListener(mupPlugin));
		register(new MoveListener(mupPlugin));
	}

	private void register(Listener listener)
	{
		Bukkit.getPluginManager().registerEvents(listener, mupPlugin);
		registeredNum++;
	}

	public int getRegisteredNum()
	{
		return registeredNum;
	}
}
