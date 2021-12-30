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
		start = System.nanoTime();
	}

	@Override
	public void endTime()
	{
		end = System.nanoTime();
	}

	@Override
	public double getTime()
	{
		return (end - start) / 1000000D;
	}

	@Override
	public double getAvg()
	{
		return getTime();
	}

	@Override
	public double getMin()
	{
		return getTime();
	}

	@Override
	public double getMax()
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
