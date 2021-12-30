package mup.nolan.mupplugin;

import mup.nolan.mupplugin.accounts.AccountManager;
import mup.nolan.mupplugin.commands.CommandManager;
import mup.nolan.mupplugin.config.ConfigManager;
import mup.nolan.mupplugin.db.MupDB;
import mup.nolan.mupplugin.hooks.Hooks;
import mup.nolan.mupplugin.listeners.ListenerManager;
import mup.nolan.mupplugin.modules.ModuleManager;
import mup.nolan.mupplugin.utils.meter.TurboMeter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.util.logging.Logger;

public final class MupPlugin extends JavaPlugin
{
	private static MupPlugin inst;

	private ConfigManager configManager;
	private MupDB mupdb;
	private AccountManager accountManager;
	private ModuleManager moduleManager;
	private ListenerManager listenerManager;
	private CommandManager commandManager;

	@Override
	public void onEnable()
	{
		inst = this;

		TurboMeter.start("init");

		configManager = new ConfigManager(this);
		configManager.loadConfigs();

		mupdb = new MupDB(configManager.getConfig("db"));
		mupdb.connect();

		accountManager = new AccountManager(this);
		accountManager.loadAccounts();

		moduleManager = new ModuleManager(this);
		moduleManager.registerModules();

		listenerManager = new ListenerManager(this);
		listenerManager.registerListeners();

		commandManager = new CommandManager(this);
		commandManager.registerCommands();

		Hooks.init();

		TurboMeter.end(true);
	}

	@Override
	public void onDisable()
	{
		TurboMeter.start("exit");

		moduleManager.disableAll();
		accountManager.saveAccounts();
		mupdb.disconnect();

		TurboMeter.end();
		TurboMeter.log("exit");
	}

	public ConfigManager getConfigManager()
	{
		return configManager;
	}

	public MupDB getDB()
	{
		return mupdb;
	}

	public AccountManager getAccountManager()
	{
		return accountManager;
	}

	public ModuleManager getModuleManager()
	{
		return moduleManager;
	}

	public ListenerManager getListenerManager()
	{
		return listenerManager;
	}

	public CommandManager getCommandManager()
	{
		return commandManager;
	}

	public static MupPlugin get()
	{
		return inst;
	}

	public static Logger log()
	{
		return inst.getLogger();
	}

	public static InputStream getRes(String path)
	{
		return inst.getResource(path);
	}
}
