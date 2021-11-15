package mup.nolan.mupplugin.db;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.utils.FileUtils;

import java.sql.*;

public class MupDB
{
	private final Config dbConfig;
	private Connection conn;

	public MupDB(Config dbConfig)
	{
		this.dbConfig = dbConfig;
	}

	public void connect()
	{
		final String type = dbConfig.getString("type");
		final String address = dbConfig.getString("address");
		final String user = dbConfig.getString("user");
		final String passwd = dbConfig.getString("passwd");
		final String dbName = dbConfig.getString("db-name");

		try
		{
			conn = type.equalsIgnoreCase("sqlite") ?
					DriverManager.getConnection(String.format("jdbc:sqlite:%s/%s", MupPlugin.get().getDataFolder().getPath(), address)) :
					DriverManager.getConnection(String.format("jdbc:mysql://%s/%s", address, dbName), user, passwd);

			final Statement stat = conn.createStatement();
			for (String s : FileUtils.readStr(MupPlugin.getRes("sql/createTables.sql")).split(";"))
				stat.execute(replaceFor(type, s));
			stat.close();

		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void disconnect()
	{
		if (conn == null)
			return;

		try
		{
			if (!conn.getAutoCommit())
				conn.commit();
			conn.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public Statement getStatement()
	{
		try
		{
			return conn.createStatement();
		} catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public String getType()
	{
		return dbConfig.getString("type");
	}

	private String replaceFor(String type, String str)
	{
		if (type.equalsIgnoreCase("sqlite"))
		{
			return str.replaceAll("auto_increment", "autoincrement");
		}
		else // mysql
		{
			return str.replaceAll("autoincrement", "auto_increment");
		}
	}

	public static void closeStatement(Statement stat)
	{
		try
		{
			stat.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
