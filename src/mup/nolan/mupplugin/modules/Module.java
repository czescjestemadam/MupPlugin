package mup.nolan.mupplugin.modules;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.utils.StrUtils;

public abstract class Module
{
	private final MupPlugin mupPlugin;
	private final String name;
	private boolean enabled = false;

	public Module(MupPlugin mupPlugin, String name)
	{
		this.mupPlugin = mupPlugin;
		this.name = name;
	}

	public void onEnable() {}

	public void onDisable() {}

	public void reload()
	{
		onDisable();
		onEnable();
		enabled = true;
		MupPlugin.log().info(StrUtils.replaceColors(mupPlugin.getConfigManager().getConfig("modules").getString("messages.on-reload").replace("{}", name)));
	}

	public String getName()
	{
		return name;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		if (this.enabled == enabled)
			return;

		if (this.enabled = enabled)
			onEnable();
		else
			onDisable();

		final String cfgStr = enabled ? "messages.on-enable" : "messages.on-disable";
		MupPlugin.log().info(StrUtils.replaceColors(mupPlugin.getConfigManager().getConfig("modules").getString(cfgStr).replace("{}", name)));
	}

	public MupPlugin getMupPlugin()
	{
		return mupPlugin;
	}
}
