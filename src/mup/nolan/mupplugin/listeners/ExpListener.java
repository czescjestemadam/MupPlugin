package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;

public class ExpListener implements Listener
{
	private final MupPlugin mupPlugin;

	public ExpListener(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
	}

	@EventHandler
	private void onExpBottle(ExpBottleEvent e)
	{
		e.setExperience(10);
	}
}
