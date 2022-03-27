package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.modules.antiafk.AntiafkModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener
{
	private final MupPlugin mupPlugin;
	private final ModuleManager mm;

	private final AntiafkModule antiafk;

	public MoveListener(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
		mm = mupPlugin.getModuleManager();

		antiafk = (AntiafkModule)mm.getModule("antiafk");
	}

	@EventHandler
	private void onPlayerMove(PlayerMoveEvent e)
	{
		antiafk.move(e);
	}
}
