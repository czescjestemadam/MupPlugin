package mup.nolan.mupplugin.modules.discord;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.db.DiscordLink;
import org.bukkit.Bukkit;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;

import java.util.Optional;

public class DiscordBot
{
	private final DiscordModule module;
	private final Config cfg;
	private DiscordApi api;

	public DiscordBot(DiscordModule module, Config cfg)
	{
		this.module = module;
		this.cfg = cfg;
	}

	void start(Runnable onReady)
	{
		Bukkit.getScheduler().runTaskAsynchronously(MupPlugin.get(), () -> {
			final String token = cfg.getString("bot.token");
			if (token.equalsIgnoreCase("tokenok"))
			{
				MupPlugin.log().severe("Change your bot token in discord.yml");
				module.setEnabled(false);
				return;
			}

			new DiscordApiBuilder()
					.addListener(new DiscordListener(this, cfg))
					.setToken(token)
					.login()
					.thenAccept(api -> {
						this.api = api;
						onReady.run();
					})
					.exceptionally(thr -> {
						MupPlugin.log().severe("Change your bot token in discord.yml");
						return null;
					});
		});
	}

	void stop()
	{
		if (api != null)
			api.disconnect();
	}

	public void sendMessage(String channelName, String message)
	{
		getChannel(channelName).ifPresent(c -> c.sendMessage(message));
	}

	public void sendVerificationMessage(DiscordLink link)
	{
		if (api == null)
			return;

		api.getUserById(link.getDiscordId()).thenAcceptAsync(u -> {
			u.getPrivateChannel().or(() -> Optional.ofNullable(u.openPrivateChannel().join())).ifPresent(c -> {
				c.sendMessage(cfg.getString("messages.verification-format").replaceAll("\\{mention}", u.getMentionTag()).replaceAll("\\{code}", link.getVerificationCode()));
			});
		});
	}

	private Optional<TextChannel> getChannel(String name)
	{
		if (api == null)
			return Optional.empty();

		final String channelId = cfg.getString("channels." + name);
		if (channelId == null || channelId.isEmpty())
		{
			MupPlugin.log().warning("No channel named " + name + " in discord.yml");
			return Optional.empty();
		}

		return api.getTextChannelById(channelId);
	}
}
