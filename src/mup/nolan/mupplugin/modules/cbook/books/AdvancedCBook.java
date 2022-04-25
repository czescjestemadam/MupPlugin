package mup.nolan.mupplugin.modules.cbook.books;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedCBook extends BaseCBook
{
	public AdvancedCBook(String name)
	{
		super(name);
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
		final String raw;
		boolean returnRaw = false;
		String name;
		final Map<String, Object> props = new HashMap<>();
		final List<String> flags = new ArrayList<>();

		public Tag(String tag)
		{
			raw = tag;

			tag = tag.substring(1, tag.length() - 1).trim();
			final int nameLen = tag.indexOf(' ');
			if (nameLen < 0 || tag.isEmpty())
			{
				name = raw;
				returnRaw = true;
				return;
			}

			name = tag.substring(0, nameLen);
			tag = tag.substring(nameLen + 1);

			boolean qs = false;
			boolean val = false;
			String buff = "";
			String prop = "";
			for (char c : tag.toCharArray())
			{
				if (c == '\'' || c == '"')
					qs = !qs;

				if (qs)
					buff += c;
				else if (c == '=')
				{
					prop = buff;
					buff = "";
					val = true;
				}
				else if (c == ' ')
				{
					if (val)
					{
						putProperty(buff, prop);
						prop = "";
						buff = "";
						val = false;
					}
					else
					{
						flags.add(buff);
						buff = "";
					}
				}
				else
					buff += c;
			}

			if (val)
				putProperty(buff, prop);
		}

		private void putProperty(String buff, String prop)
		{
			Object value;
			if ((buff.charAt(0) == '\'' || buff.charAt(0) == '"') && (buff.charAt(buff.length() - 1) == '\'' || buff.charAt(buff.length() - 1) == '"'))
				value = buff.substring(1, buff.length() - 1);
			else
				value = Integer.parseInt(buff);
			props.put(prop, value);
		}

		public TextComponent toComponent()
		{
			final TextComponent tx = new TextComponent();

			tx.setText((String)props.get("text"));

			switch (name)
			{
				case "link" -> link(tx);
			}

			return tx;
		}

		private void link(TextComponent tx)
		{
			tx.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, (String)props.get("url")));
			if (flags.contains("u"))
				tx.setUnderlined(true);
		}

		@Override
		public String toString()
		{
			return "Tag{" +
					"raw='" + raw + '\'' +
					", returnRaw=" + returnRaw +
					", name='" + name + '\'' +
					", props=" + props +
					", flags=" + flags +
					'}';
		}
	}
}
