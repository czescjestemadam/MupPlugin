package mup.nolan.mupplugin.utils.meter;

public class SingleMeasure implements Measure
{
	private final String name;
	private long start;
	private long end;

	public SingleMeasure(String name)
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void startTime()
	{
		start = System.currentTimeMillis();
	}

	@Override
	public void endTime()
	{
		end = System.currentTimeMillis();
	}

	@Override
	public long getTime()
	{
		return end - start;
	}

	@Override
	public long getAvg()
	{
		return getTime();
	}

	@Override
	public long getMin()
	{
		return getTime();
	}

	@Override
	public long getMax()
	{
		return getTime();
	}

	@Override
	public boolean isMulti()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "SingleMeasure{" +
				"name='" + name + '\'' +
				", start=" + start +
				", end=" + end +
				", time=" + getTime() +
				'}';
	}
}
