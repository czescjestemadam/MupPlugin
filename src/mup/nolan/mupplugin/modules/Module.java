package mup.nolan.mupplugin.modules;

import mup.nolan.mupplugin.MupPlugin;

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
		MupPlugin.log().info(mupPlugin.getConfigManager().getConfig("modules").getStringF("messages.on-reload").replace("{}", name));
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
		MupPlugin.log().info(mupPlugin.getConfigManager().getConfig("modules").getStringF(cfgStr).replace("{}", name));
	}

	public MupPlugin mup()
	{
		return mupPlugin;
	}
}
