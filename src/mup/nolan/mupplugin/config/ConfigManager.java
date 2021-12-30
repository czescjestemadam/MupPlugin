package mup.nolan.mupplugin.config;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.utils.FileUtils;
import mup.nolan.mupplugin.utils.meter.TurboMeter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		TurboMeter.start("init_config");

		final ExecutorService exec = Executors.newFixedThreadPool(4);
		exec.execute(() -> load("db"));
		exec.submit(() -> load("modules"));
		exec.submit(() -> load("placeholders"));
		exec.submit(() -> load("antiafk"));
		exec.submit(() -> load("bottlexp"));
		exec.submit(() -> load("commands"));
		exec.submit(() -> load("gallery"));
		exec.submit(() -> load("itemsort"));
		exec.submit(() -> load("cheatnono"));
		exec.submit(() -> load("chatpatrol"));

		exec.submit(() -> FileUtils.copyFile(MupPlugin.getRes("files/permissions.txt"), new File(MupPlugin.get().getDataFolder(), "permissions.txt")));
		exec.submit(() -> FileUtils.copyFile(MupPlugin.getRes("files/placeholders.txt"), new File(MupPlugin.get().getDataFolder(), "placeholders.txt")));

		TurboMeter.end(true);
	}

	public List<String> getConfigs()
	{
		return configs.stream().map(Config::getName).toList();
	}

	public Config getConfig(String config)
	{
		return configs.stream().filter(c -> c.getName().equalsIgnoreCase(config)).findFirst().orElse(null);
	}

	private void load(String name)
	{
		final Config config = new Config(name, new File(mupPlugin.getDataFolder(), name + ".yml"), mupPlugin.getResource("files/" + name + ".yml"));
		config.load();
		configs.add(config);
	}
}
