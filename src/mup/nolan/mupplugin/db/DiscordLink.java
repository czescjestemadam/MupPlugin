package mup.nolan.mupplugin.db;

import org.bukkit.OfflinePlayer;

public class DiscordLink
{
	private final OfflinePlayer player;
	private final long discordId;
	private String verificationCode;
	private boolean verified;

	public DiscordLink(OfflinePlayer player, long discordId, String verificationCode, boolean verified)
	{
		this.player = player;
		this.discordId = discordId;
		this.verificationCode = verificationCode;
		this.verified = verified;
	}

	public OfflinePlayer getPlayer()
	{
		return player;
	}

	public long getDiscordId()
	{
		return discordId;
	}

	public String getVerificationCode()
	{
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode)
	{
		this.verificationCode = verificationCode;
	}

	public boolean isVerified()
	{
		return verified;
	}

	public void setVerified(boolean verified)
	{
		this.verified = verified;
	}

	@Override
	public String toString()
	{
		return "DiscordLink{" +
				"player=" + player +
				", discordId=" + discordId +
				", verificationCode='" + verificationCode + '\'' +
				", verified=" + verified +
				'}';
	}
}
