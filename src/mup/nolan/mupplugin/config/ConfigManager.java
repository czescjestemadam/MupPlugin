package mup.nolan.mupplugin.config;

import mup.nolan.mupplugin.MupPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager
{
	private final MupPlugin mupPlugin;
	private final List<Config> configs = new ArrayList<>();

	public ConfigManager(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
	}

	public void loadConfigs()
	{
		load("antiafk");
		load("butelka");
		load("commands");
		load("db");
		load("gallery");
		load("itemsort");
		load("modules");
		load("placeholders");
		load("cheatnono");
	}

	public List<String> getConfigs()
	{
		return configs.stream().map(Config::getName).toList();
	}

	public Config getConfig(String config)
	{
		return configs.stream().filter(c -> c.getName().equalsIgnoreCase(config)).findFirst().orElse(null);
	}

	public void reload(String config)
	{
		getConfig(config).load();
	}

	private void load(String name)
	{
		final Config config = new Config(name, new File(mupPlugin.getDataFolder(), name + ".yml"), mupPlugin.getResource("files/" + name + ".yml"));
		config.load();
		configs.add(config);
	}
}
