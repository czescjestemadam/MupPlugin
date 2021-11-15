package mup.nolan.mupplugin.modules;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiafkModule extends Module
{
	private final MupPlugin mupPlugin;
	private final Map<Player, Long> lastMovePithc = new HashMap<>();
	private final Map<Player, Long> lastMoveYaw = new HashMap<>();
	private final Map<Player, Long> lastMovePos = new HashMap<>();
	private BukkitRunnable runnable;

	public AntiafkModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "antiafk");
		this.mupPlugin = mupPlugin;
	}

	@Override
	public void onEnable()
	{
		runnable = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				final List<Player> toKick = new ArrayList<>();



				toKick.forEach(AntiafkModule.this::kick);
				toKick.clear();
			}
		};

		final Config cfg = mupPlugin.getConfigManager().getConfig("antiafk");
		runnable.runTaskTimer(mupPlugin, cfg.getInt("check-interval") * 20L, cfg.getInt("check-interval") * 20L);
	}

	@Override
	public void onDisable()
	{
		if (runnable != null && !runnable.isCancelled())
			runnable.cancel();
	}

	public void move(PlayerMoveEvent e)
	{
		if (!this.isEnabled() || e.getTo() == null)
			return;

		if (e.getFrom().getPitch() != e.getTo().getPitch())
			lastMovePithc.put(e.getPlayer(), System.currentTimeMillis());

		if (e.getFrom().getYaw() != e.getTo().getYaw())
			lastMoveYaw.put(e.getPlayer(), System.currentTimeMillis());

		if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY() || e.getFrom().getBlockZ() != e.getTo().getBlockZ())
			lastMovePos.put(e.getPlayer(), System.currentTimeMillis());
	}

	private void warn(Player player)
	{

	}

	private void kick(Player player)
	{

	}
}
