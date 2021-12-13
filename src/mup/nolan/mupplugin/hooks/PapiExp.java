package mup.nolan.mupplugin.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import org.bukkit.entity.Player;

public class PapiExp extends PlaceholderExpansion
{
	@Override
	public String onPlaceholderRequest(Player player, String params)
	{
		final Config plcfg = MupPlugin.get().getConfigManager().getConfig("placeholders");

		if (params.equalsIgnoreCase("vanished"))
			return VanishHook.isVanished(player) ? plcfg.getString("format.vanished-on") : plcfg.getString("format.vanished-off");

		return "oop";
	}

	@Override
	public String getIdentifier()
	{
		return "mup";
	}

	@Override
	public String getAuthor()
	{
		return MupPlugin.get().getDescription().getAuthors().get(0);
	}

	@Override
	public String getVersion()
	{
		return MupPlugin.get().getDescription().getVersion();
	}
}
