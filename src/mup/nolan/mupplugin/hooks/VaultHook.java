package mup.nolan.mupplugin.hooks;

import mup.nolan.mupplugin.MupPlugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook
{
	private static Permission perms;
	private static Chat chat;
	private static Economy eco;

	public static void init()
	{
		perms = (Permission)getProvider(Permission.class);
		chat = (Chat)getProvider(Chat.class);
		eco = (Economy)getProvider(Economy.class);
	}

	public static boolean hasPerms()
	{
		return perms != null;
	}

	public static Permission getPerms()
	{
		return perms;
	}

	public static boolean hasChat()
	{
		return chat != null;
	}

	public static Chat getChat()
	{
		return chat;
	}

	public static boolean hasEco()
	{
		return eco != null;
	}

	public static Economy getEco()
	{
		return eco;
	}

	private static <T> Object getProvider(Class<T> cl)
	{
		final RegisteredServiceProvider<T> rsp = Bukkit.getServicesManager().getRegistration(cl);
		if (rsp == null)
		{
			MupPlugin.log().severe(cl.getSimpleName() + " vault service provider is not active");
			return null;
		}
		return rsp.getProvider();
	}
}
