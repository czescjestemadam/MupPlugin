package mup.nolan.mupplugin.modules;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.antiafk.AntiafkModule;
import mup.nolan.mupplugin.modules.chatpatrol.ChatPatrolModule;
import mup.nolan.mupplugin.modules.discord.DiscordModule;
import mup.nolan.mupplugin.modules.gallery.GalleryModule;
import mup.nolan.mupplugin.modules.reports.ReportsModule;
import mup.nolan.mupplugin.utils.meter.TurboMeter;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager
{
	private final MupPlugin mupPlugin;
	private final List<Module> modules = new ArrayList<>();

	public ModuleManager(MupPlugin mupPlugin)
	{
		this.mupPlugin = mupPlugin;
	}

	public void registerModules()
	{
		TurboMeter.start("init_modules");

		register(ItemsortModule.class);
		register(BottlexpModule.class);
		register(GalleryModule.class);
		register(AntiafkModule.class);
		register(CheatnonoModule.class);
		register(ChatPatrolModule.class);
		register(DiscordModule.class);
		register(ReportsModule.class);
		register(ChestshopFix.class);

		TurboMeter.end(MupPlugin.DEBUG > 0);
	}

	public List<String> getModules(boolean enabledOnly)
	{
		return modules.stream().filter(m -> !enabledOnly || m.isEnabled()).map(Module::getName).toList();
	}

	public Module getModule(String moduleName)
	{
//		return modules.stream().filter(m -> m.getName().equalsIgnoreCase(moduleName)).findFirst().orElse(null);
		for (Module mod : modules)
		{
			if (mod.getName().equalsIgnoreCase(moduleName))
				return mod;
		}
		return null;
	}

	public boolean isModuleEnabled(String moduleName)
	{
		return getModule(moduleName) != null && getModule(moduleName).isEnabled();
	}

	public boolean checkEnabled(String moduleName, CommandSender sender)
	{
		if (isModuleEnabled(moduleName))
			return false;
		sender.sendMessage(mupPlugin.getConfigManager().getConfig("modules").getStringF("messages.on-command-disabled").replace("{}", moduleName));
		return true;
	}

	public void disableAll()
	{
		modules.forEach(m -> m.setEnabled(false));
	}

	private void register(Class<? extends Module> moduleClass)
	{
		final Module module;

		try
		{
			module = moduleClass.getConstructor(MupPlugin.class).newInstance(mupPlugin);
		} catch (Exception e)
		{
			MupPlugin.log().severe("Error constructing module class " + moduleClass.getName());
			e.printStackTrace();
			return;
		}

		if (mupPlugin.getConfigManager().getConfig("modules").getBool(module.getName()))
			module.setEnabled(true);
		modules.add(module);
	}
}
