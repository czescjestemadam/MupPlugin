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
		TurboMeter.end(true);
	}

	public void saveAccounts()
	{

	}
}
