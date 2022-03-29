package mup.nolan.mupplugin.modules.nogrief.regionrollback;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockSnapshot
{
	private final Block block;
	private final Material type;
	private final BlockData blockData;

	public BlockSnapshot(Block block)
	{
		this.block = block;
		type = block.getType();
		blockData = block.getBlockData().clone();
	}

	public BlockSnapshot(Block block, Material type, BlockData blockData)
	{
		this.block = block;
		this.type = type;
		this.blockData = blockData.clone();
	}

	public Block getBlock()
	{
		return block;
	}

	public void restore()
	{
		block.setType(type);
		block.setBlockData(blockData);
	}

	@Override
	public String toString()
	{
		return "BlockSnapshot{" +
				"block=" + block +
				", type=" + type +
				", blockData=" + blockData +
				'}';
	}
}
