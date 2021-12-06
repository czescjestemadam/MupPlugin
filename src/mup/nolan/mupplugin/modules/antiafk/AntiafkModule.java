package mup.nolan.mupplugin.modules.antiafk;

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
	private final Config cfg;
	private final Map<Player, AntiafkMove> lastMove = new HashMap<>();
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
		lastMove.clear();
	}

	public void move(PlayerMoveEvent e)
	{
		if (!this.isEnabled() || e.getTo() == null)
			return;

		if (lastMove.containsKey(e.getPlayer()))
			lastMove.get(e.getPlayer()).move(e.getFrom(), e.getTo());
		else
			lastMove.put(e.getPlayer(), new AntiafkMove());
	}

	private void warn(Player player)
	{

	}

	private void kick(Player player)
	{

	}
}
