package mup.nolan.mupplugin.modules.antiafk;

import org.bukkit.Location;

public class AntiafkMove
{
	private long pitch;
	private long yaw;
	private long pos;

	public AntiafkMove()
	{
		pitch = System.currentTimeMillis();
		yaw = System.currentTimeMillis();
		pos = System.currentTimeMillis();
	}

	public long getPitch()
	{
		return pitch;
	}

	public long getYaw()
	{
		return yaw;
	}

	public long getPos()
	{
		return pos;
	}

	public long last()
	{
		return Math.min(Math.min(pitch, yaw), pos);
	}

	public void move(Location from, Location to)
	{
		if (from.getPitch() != to.getPitch())
			pitch = System.currentTimeMillis();

		if (from.getYaw() != to.getYaw())
			yaw = System.currentTimeMillis();

		if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())
			pos = System.currentTimeMillis();
	}

	public String toShortString()
	{
		return "p:%d y:%d pos:%d".formatted(
				(System.currentTimeMillis() - pitch) / 1000,
				(System.currentTimeMillis() - yaw) / 1000,
				(System.currentTimeMillis() - pos) / 1000
		);
	}

	@Override
	public String toString()
	{
		return "AntiafkMove{" +
				"pitch=" + pitch +
				", yaw=" + yaw +
				", pos=" + pos +
				'}';
	}
}
