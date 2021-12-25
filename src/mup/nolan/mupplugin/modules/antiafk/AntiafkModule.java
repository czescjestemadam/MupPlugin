package mup.nolan.mupplugin.modules.antiafk;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiafkModule extends Module
{
	private final Map<Player, AntiafkMove> lastMove = new HashMap<>();
	private BukkitRunnable runnable;

	public AntiafkModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "antiafk");
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

				Bukkit.getOnlinePlayers().stream().filter(p -> !p.hasPermission("mup.antiafk.exempt")).forEach(p -> {
					if (!lastMove.containsKey(p))
						return;

					final int afkTime = (int)(System.currentTimeMillis() - lastMove.get(p).last()) / 1000;

					if (afkTime > cfg().getInt("kick.time"))
						toKick.add(p);
					else
						warn(p, afkTime);
				});

				toKick.forEach(AntiafkModule.this::kick);
				toKick.clear();
			}
		};
		runnable.runTaskTimer(mup(), cfg().getInt("check-interval") * 20L, cfg().getInt("check-interval") * 20L);
	}

	@Override
	public void onDisable()
	{
		if (runnable != null && !runnable.isCancelled())
			runnable.cancel();
		lastMove.clear();
	}

	public void onJoin(Player player)
	{
		lastMove.put(player, new AntiafkMove());
	}

	public void onQuit(Player player)
	{
		lastMove.remove(player);
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

	public AntiafkMove getLastMove(Player player)
	{
		return lastMove.get(player);
	}

	private void warn(Player player, int afkTime)
	{
		final String title = StrUtils.replaceColors((String)cfg().getNearest("warn-titles", afkTime, cfg().getInt("check-interval")));
		if (title != null)
		{
			final int splitterIndex = title.indexOf(";;");
			player.sendTitle(title.substring(0, splitterIndex).trim(), title.substring(splitterIndex + 2).trim());
		}
	}

	private void kick(Player player)
	{
		final Object val = cfg().get("kick.command");
		if (val instanceof String cmd)
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("\\{player}", player.getName()));
		else if (val instanceof List<?> cmdList)
			cmdList.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ((String)cmd).replaceAll("\\{player}", player.getName())));
		else
			player.kickPlayer("§aWyrzucono Cię z powodu AFK!");
	}
}
