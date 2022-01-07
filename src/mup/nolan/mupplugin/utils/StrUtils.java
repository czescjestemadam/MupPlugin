package mup.nolan.mupplugin.utils;

import joptsimple.internal.Strings;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils
{
	public static String replaceColors(String str)
	{
		if (str == null)
			return null;

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

	public static String roundNum(double val, int place)
	{
		final int scale = (int)Math.pow(10, place);
		return String.valueOf((double)Math.round(val * scale) / scale);
	}

	public static String discordEscaped(String str)
	{
		if (str == null)
			return "null";
		return str.replaceAll("\\*", "\\\\\\*").replaceAll("_", "\\\\\\_").replaceAll("~", "\\\\\\~").replaceAll("`", "\\\\\\`");
	}

	public static String random(int len)
	{
		return new Random().ints(48, 123)
				.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
				.limit(len)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}

	public static Date parseTimeDiff(String str, Date from)
	{
		final Date diff = (Date)from.clone();

		final Matcher m = Pattern.compile("\\d+(s|mo?|h|d|w|y)").matcher(str);
		while (m.find())
		{
			final String group = m.group();
			final int amt = Integer.parseInt(group.replaceAll("\\D+", ""));
			final String ext = group.replaceAll("\\d+", "");

			switch (ext)
			{
				case "s" -> diff.setSeconds(diff.getSeconds() + amt);
				case "m" -> diff.setMinutes(diff.getMinutes() + amt);
				case "h" -> diff.setHours(diff.getHours() + amt);
				case "d" -> diff.setDate(diff.getDate() + amt);
				case "mo" -> diff.setMonth(diff.getMonth() + amt);
				case "y" -> diff.setYear(diff.getYear() + amt);
			}
		}

		return diff;
	}

	public static String formatLocation(String format, Location loc, int round)
	{
		if (loc == null)
			return format;

		return format.replace("{x}", roundNum(loc.getX(), round))
				.replace("{y}", roundNum(loc.getY(), round))
				.replace("{z}", roundNum(loc.getZ(), round))
				.replace("{w}", loc.getWorld().getName());
	}

	public static String safeNull(String str)
	{
		return str == null ? "null" : str;
	}
}
