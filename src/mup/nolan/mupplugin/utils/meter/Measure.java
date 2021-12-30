package mup.nolan.mupplugin.utils.meter;

public interface Measure
{
	String getName();

	void startTime();

	void endTime();

	double getTime();

	double getAvg();

	double getMin();

	double getMax();

	boolean isMulti();
}
