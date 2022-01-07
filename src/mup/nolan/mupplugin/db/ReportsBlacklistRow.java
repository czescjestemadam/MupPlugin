package mup.nolan.mupplugin.db;

import org.bukkit.OfflinePlayer;

import java.util.Date;

public class ReportsBlacklistRow
{
	private final int id;
	private final OfflinePlayer player;
	private final Date applied;
	private Date expires;
	private boolean expired;

	public ReportsBlacklistRow(int id, OfflinePlayer player, Date applied, Date expires, boolean expired)
	{
		this.id = id;
		this.player = player;
		this.applied = applied;
		this.expires = expires;
		this.expired = expired;
	}

	public int getId()
	{
		return id;
	}

	public OfflinePlayer getPlayer()
	{
		return player;
	}

	public Date getApplied()
	{
		return applied;
	}

	public Date getExpires()
	{
		return expires;
	}

	public void setExpires(Date expires)
	{
		this.expires = expires;
	}

	public boolean isExpired()
	{
		return expired;
	}

	public void setExpired(boolean expired)
	{
		this.expired = expired;
	}

	public boolean checkExpired()
	{
		if (expires == null)
			return false;

		if (expires.before(new Date()))
			expired = true;

		return expired;
	}

	@Override
	public String toString()
	{
		return "ReportsBlacklistRow{" +
				"id=" + id +
				", player=" + player +
				", applied=" + applied +
				", expires=" + expires +
				", expired=" + expired +
				'}';
	}
}
