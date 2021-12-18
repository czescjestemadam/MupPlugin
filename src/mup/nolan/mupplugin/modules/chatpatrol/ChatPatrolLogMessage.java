package mup.nolan.mupplugin.modules.chatpatrol;

import org.bukkit.OfflinePlayer;

public class ChatPatrolLogMessage
{
	final OfflinePlayer sender;
	final String content;
	final long timestamp = System.currentTimeMillis();

	public ChatPatrolLogMessage(OfflinePlayer sender, String content)
	{
		this.sender = sender;
		this.content = content;
	}

	@Override
	public String toString()
	{
		return "ChatPatrolLogMessage{" +
				"sender=" + sender +
				", content='" + content + '\'' +
				", timestamp=" + timestamp +
				'}';
	}
}
