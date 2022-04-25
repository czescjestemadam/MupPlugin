package mup.nolan.mupplugin.modules.cbook.books;

import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class SimpleCBook extends BaseCBook
{
	public SimpleCBook(String name)
	{
		super(name);
	}

	@Override
	public void setPages(List<String> pages)
	{
		if (pages == null || pages.size() == 0)
			return;

		final BookMeta meta = (BookMeta)book.getItemMeta();
		meta.setPages(pages.stream().map(StrUtils::replaceColors).toList());
		book.setItemMeta(meta);
	}
}
