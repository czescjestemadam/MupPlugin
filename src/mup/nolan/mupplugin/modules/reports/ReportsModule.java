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
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class ReportsModule extends Module
{
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
			if (reports == 0)
				return;

			Bukkit.broadcast(cfg().getStringF("messages.reminder").replace("{}", String.valueOf(reports)), "mup.report.reminder");
		});
	}

	@Override
	public void onDisable()
	{

	}

	public void onJoin(Player player)
	{
		if (cfg().getBool("reminder.on-join") && player.hasPermission("mup.report.reminder"))
			player.sendMessage(cfg().getStringF("messages.reminder").replace("{}", String.valueOf(reports)));
	}

	public void report(Player from, String type, OfflinePlayer player, Location pos, String comment)
	{
		if (!this.isEnabled())
			return;

		final ReportsRow row = new ReportsRow(-1, from, type, player, pos, comment, new Date(), false);
		row.setId(mup().getDB().insertReport(row));
		notify(row, "report", null);
		from.sendMessage(cfg().getStringF("messages.sent").replaceAll("\\{}", String.valueOf(row.getId())));

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

	public void checkedReports(List<ReportsRow> rows)
	{
		rows.forEach(ReportsRow::check);
		mup().getDB().updateReport(rows);
	}

	public void removeReports(List<ReportsRow> rows)
	{
		mup().getDB().removeReports(rows);
	}

	public void notify(ReportsRow row, String type, Player op)
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
		discord.getBot().sendMessage(channel, checkedPlaceholders(discordMsg, row, op));
	}

	public void sendFormatedReport(CommandSender sender, List<ReportsRow> rows)
	{
		for (ReportsRow row : rows)
		{
			if (sender instanceof Player)
			{
				final TextComponent msg = new TextComponent(placeholders(cfg().getStringF("notification.report.format"), row));
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(placeholders(cfg().getStringF("notification.report.hover"), row))));
				msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, placeholders(cfg().getString("notification.report.click"), row)));
				sender.spigot().sendMessage(msg);
			}
			else
				sender.sendMessage(placeholders(cfg().getStringF("notification.report.format"), row) + "; " + placeholders(cfg().getStringF("notification.report.hover"), row));
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

	private String checkedPlaceholders(String fmt, ReportsRow row, Player op)
	{
		return placeholders(fmt, row).replace("{operator}", StrUtils.safeNull(FuncUtils.optionallyMap(op, HumanEntity::getName)))
				.replace("{operator_e}", StrUtils.discordEscaped(FuncUtils.optionallyMap(op, HumanEntity::getName)));
	}
}
