package lv.n3o.swapper2;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class SwapperAutorun extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		Boolean autorun = settings.getBoolean("startup", true);
		if (!autorun) {
			return;
		}
		// Toast.makeText(context, "Swapper starting...", 100).show();
		SwapperCommands sc = new SwapperCommands(context);
		sc.swappiness();
		sc.swapOn();
	}

}
