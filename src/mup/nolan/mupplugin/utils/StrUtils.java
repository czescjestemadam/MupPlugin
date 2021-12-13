package mup.nolan.mupplugin.utils;

import joptsimple.internal.Strings;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StrUtils
{
	public static String replaceColors(String str)
	{
		final Matcher matcher = Pattern.compile("&([0-9a-f]|k|l|m|n|o|r|#[0-9a-f]{6})", Pattern.CASE_INSENSITIVE).matcher(str);

		while (matcher.find())
		{
			final String found = matcher.group();
			if (found.length() == 2)
				str = str.replace(found, found.replace('&', '§'));
			else
				str = str.replace(found, "§x" + Strings.join(found.substring(2).chars().mapToObj(c -> "§" + (char)c).toList(), ""));
		}

		return str;
	}

	public static List<String> returnMatches(String arg, List<String> originals)
	{
		return originals.stream().filter(s -> s.length() >= arg.length() && s.regionMatches(true, 0, arg, 0, arg.length())).toList();
	}

	public static List<Material> matchMaterialRegex(String regex)
	{
		return Arrays.stream(Material.values()).filter(m -> m.name().toLowerCase().matches(regex.toLowerCase().replaceAll("\\*", ".+"))).toList();
	}

	public static List<Material> getMaterials(List<String> list, boolean includeNulls)
	{
		final List<Material> ret = new ArrayList<>();
		list.forEach(s -> {
			if (s.startsWith("r/"))
				ret.addAll(matchMaterialRegex(s.substring(2)));
			else if (includeNulls || Material.getMaterial(s) != null)
				ret.add(Material.getMaterial(s));
		});
		return ret;
	}

	public static List<Material> getMaterials(List<String> list)
	{
		return getMaterials(list, false);
	}

	public static String capitalize(String str)
	{
		return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
	}
}
