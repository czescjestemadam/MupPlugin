package mup.nolan.mupplugin.utils.meter;

import java.util.LinkedList;
import java.util.Queue;

public class MultiMeasure implements Measure
{
	private final String name;
	private final Queue<Long> times = new LinkedList<>();
	private final int maxSize;

	public MultiMeasure(String name, int maxSize)
	{
		this.name = name;
		this.maxSize = maxSize;
	}

	public void add(long time)
	{
		times.add(time);
		if (times.size() >= maxSize)
			times.remove();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void startTime()
	{

	}

	@Override
	public void endTime()
	{

	}

	@Override
	public double getTime()
	{
		return 0;
	}

	@Override
	public double getAvg()
	{
		return 0;
	}

	@Override
	public double getMin()
	{
		return 0;
	}

	@Override
	public double getMax()
	{
		return 0;
	}

	@Override
	public boolean isMulti()
	{
		return true;
	}
}
