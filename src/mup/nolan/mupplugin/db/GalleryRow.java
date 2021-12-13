package mup.nolan.mupplugin.db;

import mup.nolan.mupplugin.utils.ItemBuilder;
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
	private int amount;
	private final Date placed;
	private String lockId;

	private boolean amountUpdate = false;

	// from db
	public GalleryRow(int id, OfflinePlayer owner, int sortNum, ItemStack item, int amount, Date placed, String lockId)
	{
		this.id = id;
		this.owner = owner;
		this.sortNum = sortNum;
		this.item = item;
		this.amount = amount;
		this.placed = placed;
		this.lockId = lockId;
	}

	// from inv
	public GalleryRow(OfflinePlayer owner, int sortNum, ItemStack item, Date placed)
	{
		this(-1, owner, sortNum, new ItemBuilder(item.clone()).withAmount(1).build(), item.getAmount(), placed, null);
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

	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int amount)
	{
		this.amount = amount;
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

	public GalleryRow withAmountUpdate()
	{
		amountUpdate = true;
		return this;
	}

	public boolean isAmountUpdate()
	{
		return amountUpdate;
	}

	@Override
	public String toString()
	{
		return "GalleryRow{" +
				"id=" + id +
				", owner=" + owner +
				", sortNum=" + sortNum +
				", item=" + item +
				", amount=" + amount +
				", placed=" + placed +
				", lockId='" + lockId + '\'' +
				", amountUpdate=" + amountUpdate +
				'}';
	}
}
