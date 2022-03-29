package mup.nolan.mupplugin.modules.nogrief;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.modules.Module;

public class NoGrief extends Module
{
	private final NoGriefRegionRollback regionRollback;

	public NoGrief(MupPlugin mupPlugin)
	{
		super(mupPlugin, "nogrief");
		regionRollback = new NoGriefRegionRollback(this);
	}

	@Override
	public void onEnable()
	{
		regionRollback.start();
	}

	@Override
	public void onDisable()
	{
		regionRollback.stop();
	}

	public NoGriefRegionRollback getRegionRollback()
	{
		return regionRollback;
	}
}
