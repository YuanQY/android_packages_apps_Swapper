package lv.n3o.swapper2;


import android.os.Bundle;
import android.preference.PreferenceActivity;


public class SwapperPreferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
