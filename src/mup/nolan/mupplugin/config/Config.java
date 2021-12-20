package mup.nolan.mupplugin.config;

import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

public class Config
{
	private final YamlConfiguration defaultCfg;
	private final String name;
	private final File file;
	private YamlConfiguration cfg;

	public Config(String name, File file, InputStream defaultConfig)
	{
		this.name = name;
		this.file = file;
		defaultCfg = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfig));

		load();
	}

	public void load()
	{
		if (file.exists())
		{
			cfg = YamlConfiguration.loadConfiguration(file);
			cfg.setDefaults(defaultCfg);
			return;
		}

		cfg = defaultCfg;
		save();
	}

	public void save()
	{
		try
		{
			cfg.save(file);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public boolean has(String path)
	{
		return cfg.contains(path, true);
	}

	public Object get(String path)
	{
		return cfg.get(path);
	}

	public Object getNearest(String path, int key, int limit)
	{
		final ConfigurationSection section = cfg.getConfigurationSection(path);
		if (section == null)
			return null;

		final Set<String> keys = section.getKeys(false);
		if (keys.isEmpty())
			return null;

		for (int i = 0; i < limit; i++)
		{
			if (keys.contains(String.valueOf(key - i)))
				return section.get(String.valueOf(key - i));
			if (keys.contains(String.valueOf(key + i)))
				return section.get(String.valueOf(key + i));
		}

		return null;
	}

	public boolean getBool(String path)
	{
		return cfg.getBoolean(path);
	}

	public int getInt(String path)
	{
		return cfg.getInt(path);
	}

	public int getInt(String path, int def)
	{
		return cfg.getInt(path, def);
	}

	public String getString(String path)
	{
		return cfg.getString(path);
	}

	public String getStringF(String path)
	{
		return StrUtils.replaceColors(cfg.getString(path));
	}

	public Material getMaterial(String path, Material def)
	{
		final Material mat = Material.getMaterial(getString(path));
		return mat == null ? def : mat;
	}

	public Material getMaterial(String path)
	{
		return getMaterial(path, Material.AIR);
	}

	public List<Material> getMaterialList(String path, boolean includeNulls)
	{
		return StrUtils.getMaterials(cfg.getStringList(path), includeNulls);
	}

	public List<Material> getMaterialList(String path)
	{
		return getMaterialList(path, false);
	}

	public String getName()
	{
		return name;
	}

	public List<String> getStringList(String path)
	{
		return cfg.getStringList(path);
	}

	public Set<String> list(String path)
	{
		return cfg.getConfigurationSection(path).getKeys(false);
	}
}
