package mup.nolan.mupplugin.modules.cbook;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.Module;
import mup.nolan.mupplugin.modules.cbook.books.AdvancedCBook;
import mup.nolan.mupplugin.modules.cbook.books.BaseCBook;
import mup.nolan.mupplugin.modules.cbook.books.CBook;
import mup.nolan.mupplugin.modules.cbook.books.SimpleCBook;
import mup.nolan.mupplugin.utils.StrUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CBookModule extends Module
{
	private final List<CBook> books = new ArrayList<>();

	public CBookModule(MupPlugin mupPlugin)
	{
		super(mupPlugin, "cbook");
	}

	@Override
	public void onEnable()
	{
		for (String bookName : cfg().list("books"))
		{
			final BaseCBook book;

			if (cfg().getBool("books." + bookName + ".advanced"))
			{
				book = new AdvancedCBook(bookName, new ItemStack(Material.WRITTEN_BOOK));
				((AdvancedCBook)book).setPages(cfg().getStringList("books." + bookName + ".pages"));
			}
			else
			{
				book = new SimpleCBook(bookName, new ItemStack(Material.WRITABLE_BOOK));
				((SimpleCBook)book).setPages(cfg().getStringList("books." + bookName + ".pages").stream().map(StrUtils::replaceColors).toList());
			}

			book.setTitle(cfg().getStringF("books." + bookName + ".title"));
			book.setAuthor(cfg().getStringF("books." + bookName + ".author"));

			books.add(book);
		}
	}

	public List<String> getBooks()
	{
		return List.copyOf(cfg().list("books"));
	}

	public CBook getBook(String name)
	{
		for (CBook book : books)
		{
			if (book.getName().equalsIgnoreCase(name))
				return book;
		}
		return null;
	}
}
