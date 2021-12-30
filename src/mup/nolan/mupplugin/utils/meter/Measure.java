package mup.nolan.mupplugin.utils.meter;

public interface Measure
{
	String getName();

	void startTime();

	void endTime();

	long getTime();

	long getAvg();

	long getMin();

	long getMax();

	boolean isMulti();
}
