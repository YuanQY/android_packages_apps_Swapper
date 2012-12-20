package lv.n3o.swapper2;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


public class SwapperSafeRemount extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		Boolean autorun = settings.getBoolean("swapperSafeRemount", true);
		if (!autorun) {
			return;
		}
		Log.d("Swapper", "Starting safe remount");
		SwapperCommands sc = new SwapperCommands(context);
		sc.swappiness();
		sc.swapOn();
	}

}
