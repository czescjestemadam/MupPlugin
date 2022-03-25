package mup.nolan.mupplugin.modules.reports;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.db.MupDB;
import mup.nolan.mupplugin.db.ReportsBlacklistRow;
import mup.nolan.mupplugin.db.ReportsRow;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.modules.discord.DiscordModule;
import mup.nolan.mupplugin.utils.FuncUtils;
import mup.nolan.mupplugin.utils.StrUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsModule extends Module
{
	private final Map<Player, Long> cooldown = new HashMap<>();
	private int reports;

	public ReportsModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "reports");
	}

	@Override
	public void onEnable()
	{
		if (!cfg().getBool("reminder.on-load"))
			return;

		mup().getDB().addOnConnect(() -> {
			if (Bukkit.getOnlinePlayers().isEmpty())
				return;

			reports = mup().getDB().getReports(-1, null, null, null, true).size();
			if (reports > 0)
				Bukkit.broadcast(cfg().getStringF("messages.reminder").replace("{}", String.valueOf(reports)), "mup.report.reminder");
		});
	}

	public void onJoin(Player player)
	{
		if (cfg().getBool("reminder.on-join") && player.hasPermission("mup.report.reminder") && reports > 0)
			player.sendMessage(cfg().getStringF("messages.reminder").replace("{}", String.valueOf(reports)));
	}

	public void onQuit(Player player)
	{
		if (!cfg().getBool("persistent-cooldown"))
			cooldown.remove(player);
	}

	public void report(Player from, String type, OfflinePlayer player, Location pos, String comment)
	{
		if (!this.isEnabled())
			return;

		if (!from.hasPermission("mup.report.cooldown-exempt") && cooldown.containsKey(from) && cooldown.get(from) + cfg().getInt("cooldown") * 1000L > System.currentTimeMillis())
		{
			final double timeLeft = cooldown.get(from) + cfg().getInt("cooldown") * 1000L - System.currentTimeMillis();
			from.sendMessage(cfg().getStringF("messages.on-cooldown").replace("{}", StrUtils.roundNum(timeLeft / 1000D, 1)));
			return;
		}
		cooldown.put(from, System.currentTimeMillis());

		final ReportsRow row = new ReportsRow(-1, from, type, player, pos, comment, new Date(), false);
		row.setId(mup().getDB().insertReport(row));
		notify(row, "report", null);
		from.sendMessage(cfg().getStringF("messages.sent").replace("{id}", String.valueOf(row.getId())));

		reports++;
	}

	public ReportsRow getReportById(int id)
	{
		final List<ReportsRow> reports = mup().getDB().getReports(id, null, null, null, false);
		if (reports.isEmpty())
			return null;
		return reports.get(0);
	}

	public List<ReportsRow> getReports(OfflinePlayer from, String type, OfflinePlayer player, boolean all)
	{
		return mup().getDB().getReports(-1, from, type, player, !all);
	}

	public void checkedReports(List<ReportsRow> rows, CommandSender op)
	{
		if (rows.isEmpty())
		{
			op.sendMessage(cfg().getStringF("messages.empty-report-set"));
			return;
		}

		rows.forEach(ReportsRow::check);
		rows.forEach(r -> notify(r, "checked", op));
		mup().getDB().updateReport(rows);
	}

	public void removeReports(List<ReportsRow> rows, CommandSender op)
	{
		if (rows.isEmpty())
		{
			op.sendMessage(cfg().getStringF("messages.empty-report-set"));
			return;
		}

		rows.forEach(ReportsRow::check);
		rows.forEach(r -> notify(r, "remove", op));
		mup().getDB().removeReports(rows);
	}

	public void notify(ReportsRow row, String type, CommandSender op)
	{
		final TextComponent notification = new TextComponent(checkedPlaceholders(cfg().getStringF("notification." + type + ".format"), row, op));
		notification.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(checkedPlaceholders(cfg().getStringF("notification." + type + ".hover"), row, op))));
		notification.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, checkedPlaceholders(cfg().getString("notification." + type + ".click"), row, op)));

		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (player.hasPermission("mup.report.notification"))
				player.spigot().sendMessage(notification);
		}

		final DiscordModule discord = (DiscordModule)mup().getModuleManager().getModule("discord");
		if (!discord.isEnabled())
			return;

		final String channel = cfg().getString("notification.discord.channel");
		if (channel == null || channel.isEmpty())
			return;

		final String discordMsg = cfg().getString("notification.discord.format." + type);
		if (discordMsg != null)
			discord.getBot().sendMessage(channel, checkedPlaceholders(discordMsg, row, op));
	}

	public void sendFormatedReport(CommandSender sender, List<ReportsRow> rows)
	{
		for (ReportsRow row : rows)
		{
			final String fmt = getPlaceholders(cfg().getStringF("notification.get.format"), row);
			final String hover = getPlaceholders(cfg().getStringF("notification.get.hover"), row);

			if (sender instanceof Player)
			{
				final TextComponent msg = new TextComponent(fmt);
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
				msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, placeholders(cfg().getString("notification.get.click"), row)));
				sender.spigot().sendMessage(msg);
			}
			else
				sender.sendMessage(fmt + "; " + hover);
		}
		sender.sendMessage("§c§l[R] §cReports: " + rows.size());
	}

	public ReportsBlacklistRow checkBlacklist(OfflinePlayer player)
	{
		final MupDB db = mup().getDB();
		final ReportsBlacklistRow blacklist = db.getReportsBlacklist(player);

		if (blacklist == null)
			return null;

		if (blacklist.checkExpired())
			db.removeReportsBlacklist(blacklist);

		return blacklist;
	}

	public void addBlacklist(OfflinePlayer player, Date expires)
	{
		mup().getDB().insertReportsBlacklist(new ReportsBlacklistRow(-1, player, new Date(), expires, false));
	}

	public void removeBlacklist(ReportsBlacklistRow row)
	{
		mup().getDB().removeReportsBlacklist(row);
	}

	private String placeholders(String fmt, ReportsRow row)
	{
		return fmt.replace("{id}", String.valueOf(row.getId()))
				.replace("{from}", StrUtils.safeNull(row.getFrom().getName()))
				.replace("{from_e}", StrUtils.discordEscaped(row.getFrom().getName()))
				.replace("{type}", row.getType())
				.replace("{type_e}", StrUtils.discordEscaped(row.getType()))
				.replace("{player}", StrUtils.safeNull(FuncUtils.optionallyMap(row.getPlayer(), OfflinePlayer::getName)))
				.replace("{player_e}", StrUtils.discordEscaped(FuncUtils.optionallyMap(row.getPlayer(), OfflinePlayer::getName)))
				.replace("{pos}", StrUtils.formatLocation("x:{x} y:{y} z:{z} @ {w}", row.getPos(), 1))
				.replace("{pos_e}", StrUtils.discordEscaped(StrUtils.formatLocation("x:{x} y:{y} z:{z} @ {w}", row.getPos(), 1)))
				.replace("{pos_tp}", StrUtils.formatLocation("{x} {y} {z}", row.getPos(), 1))
				.replace("{comment}", StrUtils.safeNull(row.getComment()))
				.replace("{comment_e}", StrUtils.discordEscaped(row.getComment()));
	}

	private String checkedPlaceholders(String fmt, ReportsRow row, CommandSender op)
	{
		return placeholders(fmt, row).replace("{operator}", StrUtils.safeNull(FuncUtils.optionallyMap(op, CommandSender::getName)))
				.replace("{operator_e}", StrUtils.discordEscaped(FuncUtils.optionallyMap(op, CommandSender::getName)));
	}

	private String getPlaceholders(String fmt, ReportsRow row)
	{
		return placeholders(fmt, row).replace("{checked}", String.valueOf(row.isChecked()))
				.replace("{sent_at}", row.getSentAt().toString());
	}
}
