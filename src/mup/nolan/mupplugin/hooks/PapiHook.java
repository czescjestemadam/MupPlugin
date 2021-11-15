package mup.nolan.mupplugin.hooks;

import org.bukkit.Bukkit;

public class PapiHook
{
	private static boolean enabled = false;

	public static void init()
	{
		if (!enabled && (enabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")))
			new PapiExp().register();
	}

	public static boolean isEnabled()
	{
		return enabled;
	}
}
