package mup.nolan.mupplugin.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileUtils
{
	public static void copyFile(InputStream in, File out)
	{
		try
		{
			Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
