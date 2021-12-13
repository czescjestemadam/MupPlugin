package mup.nolan.mupplugin.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileUtils
{
	public static void copyFile(InputStream in, File out)
	{
		try
		{
			out.createNewFile();
			Files.copy(in, out.toPath());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String readStr(InputStream in)
	{
		try
		{
			return new String(in.readAllBytes());
		} catch (IOException e)
		{
			e.printStackTrace();
			return "";
		}
	}
}
