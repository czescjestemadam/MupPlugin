package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.modules.chatpatrol.ChatPatrolModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class BlockListener implements Listener
{
	private final MupPlugin mupPlugin;
	private final ModuleManager mm;

	public BlockListener(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
		mm = mupPlugin.getModuleManager();
	}

	@EventHandler
	private void onSign(SignChangeEvent e)
	{
		((ChatPatrolModule)mm.getModule("chatpatrol")).onSign(e);
	}
}
