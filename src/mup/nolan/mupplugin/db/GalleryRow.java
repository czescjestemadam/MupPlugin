package mup.nolan.mupplugin.db;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public class GalleryRow
{
	private final int id;
	private final OfflinePlayer owner;
	private int sortNum;
	private final ItemStack item;
	private String lockId;

	public GalleryRow(int id, OfflinePlayer owner, int sortNum, ItemStack item, String lockId)
	{
		this.id = id;
		this.owner = owner;
		this.sortNum = sortNum;
		this.item = item;
		this.lockId = lockId;
	}

	public int getId()
	{
		return id;
	}

	public OfflinePlayer getOwner()
	{
		return owner;
	}

	public int getSortNum()
	{
		return sortNum;
	}

	public void setSortNum(int sortNum)
	{
		this.sortNum = sortNum;
	}

	public ItemStack getItem()
	{
		return item;
	}

	public String getLockId()
	{
		return lockId;
	}

	public void setLockId(String lockId)
	{
		this.lockId = lockId;
	}

	@Override
	public String toString()
	{
		return "GalleryRow{" +
				"id=" + id +
				", owner=" + owner +
				", sortNum=" + sortNum +
				", item=" + item +
				", lockId='" + lockId + '\'' +
				'}';
	}
}
