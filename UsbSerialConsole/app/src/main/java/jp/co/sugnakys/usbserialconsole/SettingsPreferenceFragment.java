package jp.co.sugnakys.usbserialconsole;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import jp.co.sugnakys.usbserialconsole.util.Log;

public class SettingsPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsPreferenceFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_settings);
    }

    private void setSummary() {
        String[] prefKeys = {
                getString(R.string.baudrate_key), getString(R.string.databits_key),
                getString(R.string.stopbits_key), getString(R.string.parity_key),
                getString(R.string.flowcontrol_key), getString(R.string.timestamp_format_key)
        };

        ListPreference listPref;
        for (String prefKey : prefKeys) {
            listPref = (ListPreference) findPreference(prefKey);
            Log.d(TAG, "Preference: " + prefKey + ", value: " + listPref.getEntry());
            listPref.setSummary(listPref.getEntry());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference instanceof ListPreference) {
            preference.setSummary(((ListPreference) preference).getEntry());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setSummary();
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
