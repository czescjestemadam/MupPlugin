package mup.nolan.mupplugin.modules.cbook.books;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

public class AdvancedCBook extends BaseCBook
{
	public AdvancedCBook(String name, ItemStack book)
	{
		super(name, book);
	}

	@Override
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
			final List<BaseComponent> elements = new ArrayList<>();

			boolean tag = false;
			String buff = "";

			for (char c : page.toCharArray())
			{
				if (c == '<' && !tag)
				{
					elements.add(new TextComponent(buff));
					tag = true;
				}
				else if (c == '>' && tag)
				{
					elements.add(Tag.fromString(buff).toComponent());
					tag = false;
				}

				buff += c;
			}
		}

		return interpretedPages;
	}

	private static class Tag
	{
		final String name;
		final Map<String, Object> props = new HashMap<>();

		public Tag(String name)
		{
			this.name = name;
		}

		public void addProperty(String prop, Object val)
		{
			props.put(prop, val);
		}

		public TextComponent toComponent()
		{

		}

		public static Tag fromString(String tag)
		{
			String prop;
			Object val;

			String buff = "";
			for (char c : tag.toCharArray())
			{

			}

			return new Tag("");
		}
	}
}
