package mup.nolan.mupplugin.utils;

import joptsimple.internal.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
