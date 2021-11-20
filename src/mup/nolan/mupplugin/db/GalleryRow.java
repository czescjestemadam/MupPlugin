package mup.nolan.mupplugin.db;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Date;

// create table if not exists mup_gallery (
//    id integer primary key autoincrement,
//    owner varchar(16) not null,
//    sort_num integer,
//    item blob not null,
//    placed timedate not null,
//    lock_id varchar(16)
// );

public class GalleryRow
{
	private final int id;
	private final OfflinePlayer owner;
	private int sortNum;
	private final ItemStack item;
	private final Date placed;
	private String lockId;

	public GalleryRow(int id, OfflinePlayer owner, int sortNum, ItemStack item, Date placed, String lockId)
	{
		this.id = id;
		this.owner = owner;
		this.sortNum = sortNum;
		this.item = item;
		this.placed = placed;
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

	public Date getPlaced()
	{
		return placed;
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
				", placed=" + placed +
				", lockId='" + lockId + '\'' +
				'}';
	}

	public static GalleryRow justItem(ItemStack item)
	{
		return new GalleryRow(-1, null, -1, item, null, null);
	}
}
