package mup.nolan.mupplugin.modules.nogrief.regionrollback;

public class RRBlockChange
{
	private final BlockSnapshot block;
	private final boolean placed;
	private final long timestamp;
	private long rollbackAfter;

	public RRBlockChange(BlockSnapshot block, boolean placed)
	{
		this.block = block;
		this.placed = placed;
		timestamp = System.currentTimeMillis();
	}

	public BlockSnapshot getBlock()
	{
		return block;
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
		System.out.println("block = " + block);
		block.restore();
		System.out.println("set block = " + block);
	}

	public boolean isOpposite(RRBlockChange c)
	{
		return c.block.getBlock().getLocation().equals(block.getBlock().getLocation()) && c.block.getBlock().getBlockData().matches(block.getBlock().getBlockData()) && c.placed != placed;
	}

	@Override
	public String toString()
	{
		return "RRBlockChange{" +
				"block=" + block +
				", placed=" + placed +
				", timestamp=" + timestamp +
				'}';
	}
}
