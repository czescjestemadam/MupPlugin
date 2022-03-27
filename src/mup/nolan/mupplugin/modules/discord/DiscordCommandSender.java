package mup.nolan.mupplugin.modules.discord;

import mup.nolan.mupplugin.db.DiscordLink;
import mup.nolan.mupplugin.hooks.VaultHook;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.javacord.api.entity.channel.TextChannel;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DiscordCommandSender implements CommandSender, OfflinePlayer
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
		channel.sendMessage(StrUtils.removeColors(s));
	}

	@Override
	public void sendMessage(String... strings)
	{
		channel.sendMessage(StrUtils.removeColors(String.join("\n", strings)));
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
		return VaultHook.getPerms().has((String)null, link.getPlayer().getName(), s);
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

	@Override
	public boolean isOnline()
	{
		return link.getPlayer().isOnline();
	}

	@Override
	public UUID getUniqueId()
	{
		return link.getPlayer().getUniqueId();
	}

	@Override
	public boolean isBanned()
	{
		return link.getPlayer().isBanned();
	}

	@Override
	public boolean isWhitelisted()
	{
		return link.getPlayer().isWhitelisted();
	}

	@Override
	public void setWhitelisted(boolean b)
	{
		link.getPlayer().setWhitelisted(b);
	}

	@Override
	public Player getPlayer()
	{
		return link.getPlayer().getPlayer();
	}

	@Override
	public long getFirstPlayed()
	{
		return link.getPlayer().getFirstPlayed();
	}

	@Override
	public long getLastPlayed()
	{
		return link.getPlayer().getLastPlayed();
	}

	@Override
	public boolean hasPlayedBefore()
	{
		return link.getPlayer().hasPlayedBefore();
	}

	@Override
	public Location getBedSpawnLocation()
	{
		return link.getPlayer().getBedSpawnLocation();
	}

	@Override
	public void incrementStatistic(Statistic statistic) throws IllegalArgumentException
	{
		link.getPlayer().incrementStatistic(statistic);
	}

	@Override
	public void decrementStatistic(Statistic statistic) throws IllegalArgumentException
	{
		link.getPlayer().decrementStatistic(statistic);
	}

	@Override
	public void incrementStatistic(Statistic statistic, int i) throws IllegalArgumentException
	{
		link.getPlayer().incrementStatistic(statistic, i);
	}

	@Override
	public void decrementStatistic(Statistic statistic, int i) throws IllegalArgumentException
	{
		link.getPlayer().decrementStatistic(statistic, i);
	}

	@Override
	public void setStatistic(Statistic statistic, int i) throws IllegalArgumentException
	{
		link.getPlayer().setStatistic(statistic, i);
	}

	@Override
	public int getStatistic(Statistic statistic) throws IllegalArgumentException
	{
		return link.getPlayer().getStatistic(statistic);
	}

	@Override
	public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException
	{
		link.getPlayer().incrementStatistic(statistic, material);
	}

	@Override
	public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException
	{
		link.getPlayer().decrementStatistic(statistic, material);
	}

	@Override
	public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException
	{
		return link.getPlayer().getStatistic(statistic, material);
	}

	@Override
	public void incrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException
	{
		link.getPlayer().incrementStatistic(statistic, material, i);
	}

	@Override
	public void decrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException
	{
		link.getPlayer().decrementStatistic(statistic, material, i);
	}

	@Override
	public void setStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException
	{
		link.getPlayer().setStatistic(statistic, material, i);
	}

	@Override
	public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException
	{
		link.getPlayer().incrementStatistic(statistic, entityType);
	}

	@Override
	public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException
	{
		link.getPlayer().decrementStatistic(statistic, entityType);
	}

	@Override
	public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException
	{
		return link.getPlayer().getStatistic(statistic, entityType);
	}

	@Override
	public void incrementStatistic(Statistic statistic, EntityType entityType, int i) throws IllegalArgumentException
	{
		link.getPlayer().incrementStatistic(statistic, entityType, i);
	}

	@Override
	public void decrementStatistic(Statistic statistic, EntityType entityType, int i)
	{
		link.getPlayer().decrementStatistic(statistic, entityType, i);
	}

	@Override
	public void setStatistic(Statistic statistic, EntityType entityType, int i)
	{
		link.getPlayer().setStatistic(statistic, entityType, i);
	}

	@Override
	public Map<String, Object> serialize()
	{
		return link.getPlayer().serialize();
	}
}
