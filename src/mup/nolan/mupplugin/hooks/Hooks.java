package mup.nolan.mupplugin.hooks;

import mup.nolan.mupplugin.utils.meter.TurboMeter;

public class Hooks
{
	public static void init()
	{
		TurboMeter.start("init_hooks");

		PapiHook.init();
		VaultHook.init();

		TurboMeter.end(true);
	}
}
