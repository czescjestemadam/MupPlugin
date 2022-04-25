package mup.nolan.mupplugin.modules.cbook.books;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public abstract class BaseCBook implements CBook
{
	protected final String name;
	protected final ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

	public BaseCBook(String name)
	{
		this.name = name;
	}

	public void setTitle(String title)
	{
		if (title == null)
			return;

		final BookMeta meta = (BookMeta)book.getItemMeta();
		meta.setTitle(title);
		book.setItemMeta(meta);
	}

	public void setAuthor(String author)
	{
		if (author == null)
			return;

		final BookMeta meta = (BookMeta)book.getItemMeta();
		meta.setAuthor(author);
		book.setItemMeta(meta);
	}

	public abstract void setPages(List<String> pages);

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void open(Player player)
	{
		player.openBook(book);
	}
}
