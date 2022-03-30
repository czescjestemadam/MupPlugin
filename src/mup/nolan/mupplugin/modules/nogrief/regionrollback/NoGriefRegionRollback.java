package mup.nolan.mupplugin.modules.nogrief.regionrollback;

import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.hooks.WGHook;
import mup.nolan.mupplugin.modules.nogrief.NoGrief;
import mup.nolan.mupplugin.utils.StrUtils;
import mup.nolan.mupplugin.utils.meter.TurboMeter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class NoGriefRegionRollback
{
	private final NoGrief noGrief;
	private final Config cfg;
	private final BlockingDeque<RRBlockChange> pendingChanges = new LinkedBlockingDeque<>();
	private final Object pendingLock = new Object();
	private final List<RRBlockChange> changes = new ArrayList<>();
	private final List<RRBlockChange> toRollback = new ArrayList<>();
	private BukkitRunnable pendingRunnable;
	private BukkitRunnable rollbackRunnable;

	public NoGriefRegionRollback(NoGrief noGrief, Config cfg)
	{
		this.noGrief = noGrief;
		this.cfg = cfg;
	}

	public void start()
	{
		pendingRunnable = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				while (!isCancelled())
				{
					while (!pendingChanges.isEmpty())
					{
						final RRBlockChange change = pendingChanges.poll();

						final Location loc = change.getBlock().getBlock().getLocation();
						if (!WGHook.getFlagState(loc, change.getPlayer(), (StateFlag)WGHook.getFlag("ng-region-rollback")) ||
								(cfg.getBool("region-rollback.exclude.ops") && change.getPlayer().isOp()) ||
								(cfg.getBool("region-rollback.exclude.region-members") && WGHook.isRegionsMember(loc, change.getPlayer())) ||
								(cfg.getBool("region-rollback.exclude.region-owner") && WGHook.isRegionsOwner(loc, change.getPlayer())))
							continue;

						final int time = WGHook.getFlagValI(loc, change.getPlayer(), (IntegerFlag)WGHook.getFlag("ng-region-rollback-time"));
						if (time < 1)
						{
							MupPlugin.log().warning("Flag ng-region-rollback-time at " + StrUtils.formatLocation("[{w}] {x} {y} {z}", loc, 0) + " is negative");
							continue;
						}

						change.setRollbackAfter(Math.min(time * 1000L, cfg.getInt("region-rollback.max-time")));

						if (!changes.removeIf(change::isOpposite))
							changes.add(change);
					}

					synchronized (pendingLock)
					{
						try
						{
							pendingLock.wait();
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		};
		pendingRunnable.runTaskAsynchronously(MupPlugin.get());

		rollbackRunnable = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				for (RRBlockChange c : changes)
				{
					if (c.canRollback())
						toRollback.add(c);
				}

				changes.removeAll(toRollback);

				Bukkit.getScheduler().runTask(MupPlugin.get(), () -> {
					toRollback.forEach(RRBlockChange::rollback);
					toRollback.clear();
				});
			}
		};
		rollbackRunnable.runTaskTimerAsynchronously(MupPlugin.get(), cfg.getInt("region-rollback.check-interval") * 2L, cfg.getInt("region-rollback.check-interval"));
	}

	public void stop()
	{
		if (!pendingRunnable.isCancelled())
		{
			pendingRunnable.cancel();
			synchronized (pendingLock)
			{
				pendingLock.notify();
			}
		}

		if (!rollbackRunnable.isCancelled())
			rollbackRunnable.cancel();
	}

	public void blockChange(BlockSnapshot block, Player player, boolean placed)
	{
		if (!noGrief.isEnabled())
			return;

		pendingChanges.add(new RRBlockChange(block, player, placed));
		synchronized (pendingLock)
		{
			pendingLock.notify();
		}
	}
}
