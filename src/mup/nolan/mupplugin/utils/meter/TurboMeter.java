package mup.nolan.mupplugin.utils.meter;

import mup.nolan.mupplugin.MupPlugin;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class TurboMeter
{
	private static final Stack<String> activeMeasureNames = new Stack<>();
	private static final Map<String, Measure> measures = new ConcurrentHashMap<>();

	private TurboMeter()
	{

	}

	public static void start(String measure)
	{
		start(measure, -1);
	}

	public static void start(String measure, int maxSize)
	{
		final Measure m;
		if (maxSize > 1)
		{
			if (measures.containsKey(measure))
				m = measures.get(measure);
			else
				m = new MultiMeasure(measure, maxSize);
		}
		else
			m = new SingleMeasure(measure);
		m.startTime();
		measures.put(measure, m);
		activeMeasureNames.push(measure);
	}

	public static void end()
	{
		end(false);
	}

	public static void end(boolean log)
	{
		final Measure m = measures.get(activeMeasureNames.pop());
		m.endTime();

		if (log)
			log(m.getName());
	}

	public static Measure get(String measure)
	{
		return measures.get(measure);
	}

	public static void log(String measure)
	{
		final Measure m = get(measure);
		if (m == null)
			return;

		if (m instanceof SingleMeasure)
		{
			MupPlugin.log().info("§e[TM] %s took %.3fms".formatted(m.getName(), m.getTime()));
		}
		else if (m instanceof MultiMeasure)
		{

		}
	}

	public static List<String> ls(boolean activeOnly)
	{
		if (activeOnly)
			return activeMeasureNames.stream().toList();
		return measures.keySet().stream().toList();
	}
}
