package org.tautologica.stopnotifications;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {

    public final static String KEY_PREF_LED = "pref_led";
    public final static String KEY_PREF_WIFI = "pref_wifi";
    public final static String KEY_PREF_MOBILE_DATA = "pref_mobile_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }

}
