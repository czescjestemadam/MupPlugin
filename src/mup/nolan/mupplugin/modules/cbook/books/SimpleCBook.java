package mup.nolan.mupplugin.modules.cbook.books;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class SimpleCBook extends BaseCBook
{
	public SimpleCBook(String name, ItemStack book)
	{
		super(name, book);
	}

	@Override
	public void setPages(List<String> pages)
	{
		if (pages == null || pages.size() == 0)
			return;

		final BookMeta meta = (BookMeta)book.getItemMeta();
		meta.setPages(pages);
		book.setItemMeta(meta);
	}
}
