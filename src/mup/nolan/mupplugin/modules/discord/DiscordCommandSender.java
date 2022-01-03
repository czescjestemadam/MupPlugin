package mup.nolan.mupplugin.modules.discord;

import mup.nolan.mupplugin.db.DiscordLink;
import mup.nolan.mupplugin.hooks.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.javacord.api.entity.channel.TextChannel;

import java.util.Set;
import java.util.UUID;

public class DiscordCommandSender implements CommandSender
{
	private final DiscordLink link;
	private final TextChannel channel;

	public DiscordCommandSender(DiscordLink link, TextChannel channel)
	{
		this.link = link;
		this.channel = channel;
	}

	@Override
	public void sendMessage(String s)
	{
		channel.sendMessage(s.replaceAll("§[0-9a-fklmnor]", ""));
	}

	@Override
	public void sendMessage(String... strings)
	{
		channel.sendMessage(String.join("\n", strings).replaceAll("§[0-9a-fklmnor]", ""));
	}

	@Override
	public void sendMessage(UUID uuid, String s)
	{
		sendMessage(s);
	}

	@Override
	public void sendMessage(UUID uuid, String... strings)
	{
		sendMessage(strings);
	}

	@Override
	public Server getServer()
	{
		return Bukkit.getServer();
	}

	@Override
	public String getName()
	{
		return link.getPlayer().getName();
	}

	@Override
	public Spigot spigot()
	{
		return null;
	}

	@Override
	public boolean isPermissionSet(String s)
	{
		return false;
	}

	@Override
	public boolean isPermissionSet(Permission permission)
	{
		return false;
	}

	@Override
	public boolean hasPermission(String s)
	{
		return VaultHook.getPerms().has((CommandSender)link.getPlayer(), s);
	}

	@Override
	public boolean hasPermission(Permission permission)
	{
		return hasPermission(permission.getName());
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b)
	{
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin)
	{
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i)
	{
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, int i)
	{
		return null;
	}

	@Override
	public void removeAttachment(PermissionAttachment permissionAttachment)
	{

	}

	@Override
	public void recalculatePermissions()
	{

	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions()
	{
		return null;
	}

	@Override
	public boolean isOp()
	{
		return link.getPlayer().isOp();
	}

	@Override
	public void setOp(boolean b)
	{
		link.getPlayer().setOp(b);
	}
}
