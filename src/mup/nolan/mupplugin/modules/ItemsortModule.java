package mup.nolan.mupplugin.modules;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

// create table if not exists mup_itemsort (
//    id integer primary key autoincrement,
//    enabled bool default false
// );

public class ItemsortModule extends Module
{
	private final Set<Player> reminded = new HashSet<>();

	public ItemsortModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "itemsort");
	}

	public void onChest(InventoryCloseEvent e)
	{
		if (!this.isEnabled() || !isChest(e.getView().getTopInventory()))
			return;

		final long started = System.nanoTime();
		final long startedms = System.currentTimeMillis();

		final Player p = (Player)e.getPlayer();

		if (!mup().getDB().itemsortEnabled(p))
		{
			if (reminded.add(p))
				p.sendMessage(StrUtils.replaceColors(mup().getConfigManager().getConfig("itemsort").getString("messages.reminder")));
			return;
		}

		sort(e.getInventory());

		p.sendMessage("sortowanie mup " + (System.nanoTime() - started) + "nanosekumd " + (System.currentTimeMillis() - startedms) + "ms");
	}

	public void clearReminder(Player player)
	{
		reminded.remove(player);
	}

	private boolean isChest(Inventory e)
	{
		return (e.getType() == InventoryType.BARREL || e.getType() == InventoryType.CHEST || e.getType() == InventoryType.SHULKER_BOX) && e.getHolder() != null;
	}

	private void sort(Inventory inv)
	{
		final ItemStack[] chestArr = inv.getStorageContents();

		if (Arrays.stream(chestArr).allMatch(Objects::isNull))
			return;

		for (ItemStack chestItem : chestArr)
		{
			if (chestItem == null || chestItem.getAmount() == chestItem.getMaxStackSize())
				continue;

			for (int i = Arrays.asList(chestArr).indexOf(chestItem) + 1; i < chestArr.length; i++)
			{
				if (chestArr[i] == null || chestArr[i].getAmount() == chestArr[i].getMaxStackSize() || !chestItem.isSimilar(chestArr[i]))
					continue;

				final int canAdd = chestArr[i].getAmount();
				final int needed = chestItem.getMaxStackSize() - chestItem.getAmount();
				final int adding = Math.min(canAdd, needed);

				chestArr[i].setAmount(canAdd - adding);
				chestItem.setAmount(chestItem.getAmount() + adding);

				if (canAdd > needed)
					break;
			}
		}

		inv.setStorageContents(Arrays.stream(inv.getStorageContents())
				.filter(Objects::nonNull)
				.sorted(Comparator.comparingInt(i -> i.getMaxStackSize() - i.getAmount()))
				.sorted(Comparator.comparing(i -> i.getType().getKey().getKey()))
				.toArray(ItemStack[]::new));
	}
}
