package mup.nolan.mupplugin.listeners;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.modules.chatpatrol.ChatPatrolModule;
import mup.nolan.mupplugin.modules.nogrief.regionrollback.BlockSnapshot;
import mup.nolan.mupplugin.modules.nogrief.NoGrief;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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

	@EventHandler
	private void onBlockPlace(BlockPlaceEvent e)
	{
		((NoGrief)mm.getModule("nogrief")).getRegionRollback().blockChange(new BlockSnapshot(e.getBlock(), e.getBlockReplacedState().getType(), e.getBlockReplacedState().getBlockData()), true);
	}

	@EventHandler
	private void onBlockBreak(BlockBreakEvent e)
	{
		((NoGrief)mm.getModule("nogrief")).getRegionRollback().blockChange(new BlockSnapshot(e.getBlock()), false);
	}
}
