package mup.nolan.mupplugin.modules.cbook.books;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public abstract class BaseCBook implements CBook
{
	protected final String name;
	protected final ItemStack book;

	public BaseCBook(String name, ItemStack book)
	{
		this.name = name;
		this.book = book;
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
