package mup.nolan.mupplugin.config;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.utils.FileUtils;
import mup.nolan.mupplugin.utils.meter.TurboMeter;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConfigManager
{
	private final MupPlugin mupPlugin;
	private final Lock lock = new ReentrantLock(true);
	private final List<Config> configs = new ArrayList<>();

	public ConfigManager(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
	}

	public void loadConfigs()
	{
		TurboMeter.start("init_config");

		load("db");
		load("modules");
		load("placeholders");
		load("antiafk");
		load("bottlexp");
		load("commands");
		load("gallery");
		load("itemsort");
		load("cheatnono");
		load("chatpatrol");
		load("discord");
		load("reports");

		Bukkit.getScheduler().runTaskLaterAsynchronously(mupPlugin, () -> {
			FileUtils.copyFile(MupPlugin.getRes("files/permissions.txt"), new File(MupPlugin.get().getDataFolder(), "permissions.txt"));
			FileUtils.copyFile(MupPlugin.getRes("files/placeholders.txt"), new File(MupPlugin.get().getDataFolder(), "placeholders.txt"));
		}, 60);

		TurboMeter.end(MupPlugin.DEBUG > 0);
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
		TurboMeter.start("init_config_" + name);
		final Config config = new Config(name, new File(mupPlugin.getDataFolder(), name + ".yml"), mupPlugin.getResource("files/" + name + ".yml"));
		config.load();
		lock.lock();
		configs.add(config);
		lock.unlock();
		TurboMeter.end(MupPlugin.DEBUG > 1);
	}
}
