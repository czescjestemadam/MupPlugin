package mup.nolan.mupplugin.modules.nogrief.regionrollback;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.nogrief.NoGrief;
import mup.nolan.mupplugin.utils.meter.TurboMeter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class NoGriefRegionRollback
{
	private final NoGrief noGrief;
	private final BlockingDeque<RRBlockChange> pendingChanges = new LinkedBlockingDeque<>();
	private final Object pendingLock = new Object();
	private final List<RRBlockChange> changes = new ArrayList<>();
	private final List<RRBlockChange> toRollback = new ArrayList<>();
	private BukkitRunnable pendingRunnable;
	private BukkitRunnable rollbackRunnable;

	public NoGriefRegionRollback(NoGrief noGrief)
	{
		this.noGrief = noGrief;
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

						change.setRollbackAfter(5000);

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
		rollbackRunnable.runTaskTimerAsynchronously(MupPlugin.get(), 80, 40);
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

	public void blockChange(BlockSnapshot block, boolean placed)
	{
		if (!noGrief.isEnabled())
			return;

		TurboMeter.start("nogrief_blockChange");

		pendingChanges.add(new RRBlockChange(block, placed));
		synchronized (pendingLock)
		{
			pendingLock.notify();
		}

		TurboMeter.end(true);
	}
}
