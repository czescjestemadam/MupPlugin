package mup.nolan.mupplugin.utils;

public class Resrc<T>
{
	private T resource;

	public Resrc() {}

	public Resrc(T resource)
	{
		this.resource = resource;
	}

	public T get()
	{
		return resource;
	}

	public void set(T resource)
	{
		this.resource = resource;
	}

	public boolean isNull()
	{
		return resource == null;
	}

	@Override
	public String toString()
	{
		return "Resrc{" +
				"resource=" + resource +
				'}';
	}
}
