package mup.nolan.mupplugin.modules.cbook.books;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class AdvancedCBook extends BaseCBook
{
	public AdvancedCBook(String name, ItemStack book)
	{
		super(name, book);
	}

	public void setPages(List<String> pages)
	{
		if (pages == null || pages.size() == 0)
			return;

		final BookMeta meta = (BookMeta)book.getItemMeta();
		meta.spigot().setPages(interpretPages(pages));
		book.setItemMeta(meta);
	}

	private List<BaseComponent[]> interpretPages(List<String> pages)
	{
		final List<BaseComponent[]> interpretedPages = new ArrayList<>();

		for (String page : pages)
		{

		}

		return interpretedPages;
	}
}
