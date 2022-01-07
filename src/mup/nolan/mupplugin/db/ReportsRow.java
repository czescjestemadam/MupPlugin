package mup.nolan.mupplugin.db;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.Date;

public class ReportsRow
{
	private int id;
	private final OfflinePlayer from;
	private final String type;
	private final OfflinePlayer player;
	private final Location pos;
	private final String comment;
	private final Date sent_at;
	private boolean checked;

	public ReportsRow(int id, OfflinePlayer from, String type, OfflinePlayer player, Location pos, String comment, Date sent_at, boolean checked)
	{
		this.id = id;
		this.from = from;
		this.type = type;
		this.player = player;
		this.pos = pos;
		this.comment = comment;
		this.sent_at = sent_at;
		this.checked = checked;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public OfflinePlayer getFrom()
	{
		return from;
	}

	public String getType()
	{
		return type;
	}

	public OfflinePlayer getPlayer()
	{
		return player;
	}

	public Location getPos()
	{
		return pos;
	}

	public String getComment()
	{
		return comment;
	}

	public Date getSentAt()
	{
		return sent_at;
	}

	public boolean isChecked()
	{
		return checked;
	}

	public void setChecked(boolean checked)
	{
		this.checked = checked;
	}

	public void check()
	{
		checked = true;
	}

	@Override
	public String toString()
	{
		return "ReportsRow{" +
				"id=" + id +
				", from=" + from +
				", type='" + type + '\'' +
				", player=" + player +
				", pos=" + pos +
				", comment='" + comment + '\'' +
				", sent_at=" + sent_at +
				", checked=" + checked +
				'}';
	}
}
