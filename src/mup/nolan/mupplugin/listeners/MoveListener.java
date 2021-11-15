package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.AntiafkModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener
{
	private final MupPlugin mupPlugin;

	public MoveListener(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
	}

	@EventHandler
	private void onPlayerMove(PlayerMoveEvent e)
	{
		((AntiafkModule)mupPlugin.getModuleManager().getModule("antiafk")).move(e);
	}
}
