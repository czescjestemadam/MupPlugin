package mup.nolan.mupplugin.db;

import mup.nolan.mupplugin.MupPlugin;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;

// create table if not exists mup_gallery_userdata (
//    id integer primary key autoincrement,
//    player varchar(16) not null,
//    unlocked_slots int,
//    unlocked_borders blob,
//    current_border varchar(40),
//    viewed_galleries blob,
//    liked_galleries blob
// );

public class GalleryUserdataRow
{
	private final int id;
	private final OfflinePlayer player;
	private int unlockedSlots;
	private String unlockedBorders;
	private Material currentBorder;
	private String viewedGalleries;
	private String likedGalleries;

	public GalleryUserdataRow(int id, OfflinePlayer player, int unlockedSlots, String unlockedBorders, Material currentBorder, String viewedGalleries, String likedGalleries)
	{
		this.id = id;
		this.player = player;
		this.unlockedSlots = unlockedSlots;
		this.unlockedBorders = unlockedBorders;
		this.currentBorder = currentBorder;
		this.viewedGalleries = viewedGalleries;
		this.likedGalleries = likedGalleries;
	}

	public int getId()
	{
		return id;
	}

	public OfflinePlayer getPlayer()
	{
		return player;
	}

	public int getUnlockedSlots()
	{
		return unlockedSlots;
	}

	public void setUnlockedSlots(int unlockedSlots)
	{
		this.unlockedSlots = unlockedSlots;
	}

	public void unlockSlot()
	{
		unlockedSlots++;
	}

	public String getUnlockedBorders()
	{
		return unlockedBorders;
	}

	public void setUnlockedBorders(String unlockedBorders)
	{
		this.unlockedBorders = unlockedBorders;
	}

	public boolean isBorderUnlocked(Material m)
	{
		return Arrays.stream(unlockedBorders.split(";")).anyMatch(s -> s.equalsIgnoreCase(m.name()));
	}

	public void unlockBorder(Material m)
	{
		unlockedBorders += (unlockedBorders.isEmpty() ? "" : ";") + m.name();
	}

	public Material getCurrentBorder()
	{
		return currentBorder;
	}

	public void setCurrentBorder(Material currentBorder)
	{
		this.currentBorder = currentBorder;
	}

	public String getViewedGalleries()
	{
		return viewedGalleries;
	}

	public void setViewedGalleries(String viewedGalleries)
	{
		this.viewedGalleries = viewedGalleries;
	}

	public String getLikedGalleries()
	{
		return likedGalleries;
	}

	public void setLikedGalleries(String likedGalleries)
	{
		this.likedGalleries = likedGalleries;
	}

	@Override
	public String toString()
	{
		return "GalleryUserdataRow{" +
				"id=" + id +
				", player=" + player +
				", unlockedSlots=" + unlockedSlots +
				", unlockedBorders='" + unlockedBorders + '\'' +
				", currentBorder=" + currentBorder +
				", viewedGalleries='" + viewedGalleries + '\'' +
				", likedGalleries='" + likedGalleries + '\'' +
				'}';
	}
}
