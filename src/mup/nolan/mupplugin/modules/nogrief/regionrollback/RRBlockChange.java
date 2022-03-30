package mup.nolan.mupplugin.modules.nogrief.regionrollback;

import org.bukkit.entity.Player;

public class RRBlockChange
{
	private final BlockSnapshot block;
	private final Player player;
	private final boolean placed;
	private final long timestamp;
	private long rollbackAfter;

	public RRBlockChange(BlockSnapshot block, Player player, boolean placed)
	{
		this.block = block;
		this.player = player;
		this.placed = placed;
		timestamp = System.currentTimeMillis();
	}

	public BlockSnapshot getBlock()
	{
		return block;
	}

	public Player getPlayer()
	{
		return player;
	}

	public boolean isPlaced()
	{
		return placed;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public long getRollbackAfter()
	{
		return rollbackAfter;
	}

	public void setRollbackAfter(long rollbackAfter)
	{
		this.rollbackAfter = rollbackAfter;
	}

	public boolean canRollback()
	{
		return System.currentTimeMillis() > timestamp + rollbackAfter;
	}

	public void rollback()
	{
		block.restore();
	}

	public boolean equalsLocation(RRBlockChange c)
	{
		return c.block.getBlock().getLocation().equals(block.getBlock().getLocation());
	}

	@Override
	public String toString()
	{
		return "RRBlockChange{" +
				"block=" + block +
				", player=" + player +
				", placed=" + placed +
				", timestamp=" + timestamp +
				", rollbackAfter=" + rollbackAfter +
				'}';
	}
}
