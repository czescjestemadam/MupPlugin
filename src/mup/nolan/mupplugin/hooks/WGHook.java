package mup.nolan.mupplugin.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
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
		registerFlag(new StateFlag("ng-region-rollback", false));
		registerFlag(new IntegerFlag("ng-region-rollback-time"));
	}

	private static void registerFlag(Flag<?> flag)
	{
		try
		{
			wg.getFlagRegistry().register(flag);
		} catch (FlagConflictException | IllegalStateException e)
		{
		}
	}

	public static Flag<?> getFlag(String name)
	{
		return wg.getFlagRegistry().get(name);
	}

	public static boolean getFlagState(Location loc, Player player, StateFlag flag)
	{
		final RegionContainer rc = wg.getPlatform().getRegionContainer();
		return rc.createQuery().testState(location(loc), player(player), flag);
	}

	public static Integer getFlagValI(Location loc, Player player, IntegerFlag flag)
	{
		final RegionContainer rc = wg.getPlatform().getRegionContainer();
		return rc.createQuery().queryValue(location(loc), player(player), flag);
	}

	public static boolean hasFlag(StateFlag flag)
	{
		return wg.getFlagRegistry().get(flag.getName()) != null;
	}

	public static ApplicableRegionSet getRegions(Location loc)
	{
		return wg.getPlatform().getRegionContainer().createQuery().getApplicableRegions(location(loc));
	}

	public static boolean isRegionsOwner(Location loc, Player player)
	{
		return getRegions(loc).isOwnerOfAll(player(player));
	}

	public static boolean isRegionsMember(Location loc, Player player)
	{
		return getRegions(loc).isMemberOfAll(player(player));
	}

	public static LocalPlayer player(Player player)
	{
		return WorldGuardPlugin.inst().wrapPlayer(player);
	}

	public static com.sk89q.worldedit.util.Location location(Location loc)
	{
		return BukkitAdapter.adapt(loc);
	}
}