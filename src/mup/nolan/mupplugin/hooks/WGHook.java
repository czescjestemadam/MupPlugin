package mup.nolan.mupplugin.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import mup.nolan.mupplugin.MupPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WGHook
{
	public static final StateFlag unbreakableAnvils = new StateFlag("unbreakable-anvils", false);

	private static WorldGuard wg;

	public static void init()
	{
		wg = WorldGuard.getInstance();
		addFlags();
	}

	private static void addFlags()
	{
		registerFlag(unbreakableAnvils);
	}

	private static void registerFlag(Flag<?> flag)
	{
		try
		{
			wg.getFlagRegistry().register(flag);
		} catch (FlagConflictException e)
		{
			MupPlugin.log().warning("Flag " + flag.getName() + " exists");
		} catch (IllegalStateException e)
		{
			MupPlugin.log().severe("New flags cannot be registered at this time");
		}
	}

	public static boolean getFlagState(Location loc, Player player, StateFlag flag)
	{
		final RegionContainer rc = wg.getPlatform().getRegionContainer();
		return rc.createQuery().testState(BukkitAdapter.adapt(loc), WorldGuardPlugin.inst().wrapPlayer(player), flag);
	}

	public static boolean hasFlag(StateFlag flag)
	{
		return wg.getFlagRegistry().get(flag.getName()) != null;
	}
}