package jp.sugnakys.usbserialconsole.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;

import jp.sugnakys.usbserialconsole.R;
import jp.sugnakys.usbserialconsole.util.Util;

public class DisplayPreferenceFragment extends BasePreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_display_preference);

        listPrefKeys = new String[]{getString(R.string.screen_orientation_key)};
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);

        if (key.equals(getString(R.string.screen_orientation_key))) {
            ListPreference listPref = (ListPreference) findPreference(key);
            Util.setScreenOrientation(listPref.getValue(), getActivity());
        }
    }
}
