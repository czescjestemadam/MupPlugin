package mup.nolan.mupplugin.utils;

import java.util.function.Function;

public class FuncUtils
{
	public static <T, R> R optionallyMap(T obj, Function<T, R> func)
	{
		if (obj == null)
			return null;
		return func.apply(obj);
	}
}
