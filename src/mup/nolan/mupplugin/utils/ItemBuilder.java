package mup.nolan.mupplugin.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class ItemBuilder
{
	private final ItemStack is;

	public ItemBuilder(ItemStack itemStack)
	{
		is = itemStack;
	}

	public ItemBuilder(Material material)
	{
		is = new ItemStack(material);
	}

	public ItemBuilder withName(String name)
	{
		final ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
		return this;
	}

	public ItemBuilder withLore(String... lore)
	{
		withLore(Arrays.asList(lore));
		return this;
	}

	public ItemBuilder withLore(List<String> lore)
	{
		final ItemMeta im = is.getItemMeta();
		im.setLore(lore);
		is.setItemMeta(im);
		return this;
	}

	public ItemBuilder addLore(List<String> lore)
	{
		final ItemMeta im = is.getItemMeta();
		im.getLore().addAll(lore); // todo check
		is.setItemMeta(im);
		return this;
	}

	public ItemBuilder withAmount(int amt)
	{
		is.setAmount(amt);
		return this;
	}

	public ItemBuilder addEnchantGlint()
	{
		final ItemMeta im = is.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		im.addEnchant(Enchantment.CHANNELING, 1, false);
		is.setItemMeta(im);
		return this;
	}

	public ItemStack build()
	{
		return is;
	}

	@Override
	public String toString()
	{
		return "ItemBuilder{" +
				"is=" + is +
				'}';
	}

	public static String toString(ItemStack is)
	{
		try
		{
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final BukkitObjectOutputStream bout = new BukkitObjectOutputStream(out);
			bout.writeObject(is);
			bout.close();
			return Base64.getEncoder().encodeToString(out.toByteArray());
		} catch (IOException e)
		{
			e.printStackTrace();
			return "error converting";
		}
	}

	public static ItemStack fromString(String str)
	{
		try
		{
			final ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(str));
			final BukkitObjectInputStream bin = new BukkitObjectInputStream(in);
			final ItemStack is = (ItemStack)bin.readObject();
			bin.close();
			return is;
		} catch (IOException | ClassNotFoundException e)
		{
			e.printStackTrace();
			return new ItemBuilder(Material.OAK_BUTTON).withName("§c§oerror converting").build();
		}
	}

	public static ItemBuilder builderFromString(String str)
	{
		return new ItemBuilder(fromString(str));
	}
}
