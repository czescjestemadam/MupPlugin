package mup.nolan.mupplugin.db;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.config.Config;
import mup.nolan.mupplugin.utils.FileUtils;
import mup.nolan.mupplugin.utils.ItemBuilder;
import mup.nolan.mupplugin.utils.Resrc;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

	public void getGalleryData(OfflinePlayer owner, boolean editmode, Resrc<List<GalleryRow>> items, Resrc<GalleryUserdataRow> userdata)
	{
		if (items.isNull())
			items.set(new ArrayList<>());

		final Statement st = getStatement();
		try
		{
			ResultSet rs = st.executeQuery("select * from mup_gallery where owner = '" + owner.getName() + "' " + (editmode ? "and lock_id is null" : "") + " order by sort_num");
			while (rs.next())
				items.get().add(new GalleryRow(rs.getInt("id"), owner, rs.getInt("sort_num"), ItemBuilder.fromString(rs.getString("item")), rs.getDate("placed"), rs.getString("lock_id")));

			rs = st.executeQuery("select " + (editmode ? "*" : "unlocked_slots, current_border") + " from mup_gallery_userdata where player = '" + owner.getName() + "'");
			if (rs.next())
			{
				final int slots = rs.getInt("unlocked_slots");

				userdata.set(editmode ?
						new GalleryUserdataRow(rs.getInt("id"), owner, slots, rs.getString("unlocked_borders"), Material.getMaterial(rs.getString("current_border")), null, null) :
						new GalleryUserdataRow(-1, owner, slots, null, Material.getMaterial(rs.getString("current_border")), null, null));
			}

			if (userdata.isNull())
				userdata.set(new GalleryUserdataRow(-1, owner, 0, "", MupPlugin.get().getConfigManager().getConfig("gallery").getMaterial("gui-items.default-border"), null, null));
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		closeStatement(st);
	}

	public void updateGalleryData(List<GalleryRow> rows)
	{
		final Statement st = getStatement();
		try
		{
			for (GalleryRow row : rows)
				st.addBatch("update mup_gallery set sort_num = " + row.getSortNum() + " where id = " + row.getId());
			st.executeBatch();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		closeStatement(st);
	}

	public void insertGalleryData(List<GalleryRow> rows)
	{
		final Statement st = getStatement();
		try
		{
			for (GalleryRow row : rows)
			{
				st.addBatch(String.format(
						"insert into mup_gallery values(null, '%s', %d, '%s', %d, null)",
						row.getOwner().getName(),
						row.getSortNum(),
						ItemBuilder.toString(row.getItem()),
						System.currentTimeMillis()
				));
			}
			st.executeBatch();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		closeStatement(st);
	}

	public void deleteGalleryData(List<GalleryRow> rows)
	{
		final Statement st = getStatement();
		try
		{
			for (GalleryRow row : rows)
				st.addBatch("delete from mup_gallery where id = " + row.getId());
			st.executeBatch();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		closeStatement(st);
	}

	public void updateGalleryUserdata(GalleryUserdataRow userdata)
	{

	}

	public void insertGalleryUserdata(GalleryUserdataRow userdata)
	{

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
