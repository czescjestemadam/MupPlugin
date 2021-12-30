package mup.nolan.mupplugin.utils.meter;

import mup.nolan.mupplugin.MupPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TurboMeter
{
	private static final Stack<Measure> activeMeasures = new Stack<>();
	private static final List<Measure> measures = new ArrayList<>();

	private TurboMeter()
	{

	}

	public static void start(String measure)
	{
		start(measure, -1);
	}

	public static void start(String measure, int maxSize)
	{
		if (measures.stream().anyMatch(m -> m.getName().equals(measure) && !m.isMulti()))
			throw new IllegalArgumentException("Measure " + measure + " already exists");

		final Measure m;
		if (maxSize > 1)
			m = new MultiMeasure(measure, maxSize);
		else
			m = new SingleMeasure(measure);
		m.startTime();
		activeMeasures.push(m);
	}

	public static void end()
	{
		end(false);
	}

	public static void end(boolean log)
	{
		final Measure m = activeMeasures.pop();
		m.endTime();
		measures.add(m);

		if (log)
			log(m.getName());
	}

	public static Measure get(String measure)
	{
		return measures.stream().filter(m -> m.getName().equals(measure)).findFirst().orElse(null);
	}

	public static void log(String measure)
	{
		final Measure m = get(measure);
		if (m == null)
			return;

		if (m instanceof SingleMeasure)
		{
			MupPlugin.log().info("§e[TM] %s took %dms".formatted(m.getName(), m.getTime()));
		}
		else if (m instanceof MultiMeasure)
		{

		}
	}

	public static List<String> ls()
	{
		return measures.stream().map(Measure::getName).toList();
	}
}
