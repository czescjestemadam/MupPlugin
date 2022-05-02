package mup.nolan.mupplugin.modules.discord.commands;

import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;

public class DTpsCommand extends DCommand
{
	public DTpsCommand()
	{
		super("tps", null);
	}

	@Override
	public void execute(Message og, MessageAuthor sender, List<String> args)
	{
		try
		{
			final Server mc = Bukkit.getServer();
			final Field console = mc.getClass().getDeclaredField("console");
			console.setAccessible(true);

			final Object server = console.get(mc);
			final Field tps = server.getClass().getSuperclass().getDeclaredField("recentTps");
			tps.setAccessible(true);

			final double[] values = (double[])tps.get(server);
			final String[] times = { "5s", "1m", "5m", "15m" };

			final StringBuilder msg = new StringBuilder("online: " + Bukkit.getOnlinePlayers().size() + "\n");
			for (int i = 0; i < 4; i++)
				msg.append(times[i]).append(": ").append(StrUtils.roundNum(values[i], 3)).append("\n");

			og.getChannel().sendMessage(msg.toString());
		} catch (NoSuchFieldException | IllegalAccessException e)
		{
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			final PrintStream stream = new PrintStream(os);
			e.printStackTrace(stream);
			og.getChannel().sendMessage(os.toString());
		}
	}
}
