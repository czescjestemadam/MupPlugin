package mup.nolan.mupplugin.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WGHook
{
	private static WorldGuard wg;

	public static void init()
	{
		wg = WorldGuard.getInstance();
		addFlags();
	}

	private static void addFlags()
	{
		registerFlag(new StateFlag("unbreakable-anvils", false));
	}

	private static void registerFlag(Flag<?> flag)
	{
		if (!hasFlag((StateFlag)getFlag(flag.getName())))
			wg.getFlagRegistry().register(flag);
	}

	public static Flag<?> getFlag(String name)
	{
		return wg.getFlagRegistry().get(name);
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