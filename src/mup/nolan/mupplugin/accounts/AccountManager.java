package mup.nolan.mupplugin.accounts;

import mup.nolan.mupplugin.MupPlugin;
import mup.nolan.mupplugin.utils.meter.TurboMeter;

public class AccountManager
{
	public AccountManager(MupPlugin mupPlugin)
	{

	}

	public void loadAccounts()
	{
		TurboMeter.start("init_accounts");
		TurboMeter.end(MupPlugin.DEBUG > 0);
	}

	public void saveAccounts()
	{

	}
}
