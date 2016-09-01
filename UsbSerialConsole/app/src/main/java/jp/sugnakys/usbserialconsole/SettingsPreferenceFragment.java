package jp.sugnakys.usbserialconsole;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.sugnakys.usbserialconsole.util.Log;
import jp.sugnakys.usbserialconsole.util.Util;

public class SettingsPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsPreferenceFragment";

    private static List<String> prefSerialList;
    private static String[] listPrefKeys;

    private ListPreference timestampFormatPref;
    private ListPreference lineFeedCodePref;
    private CheckBoxPreference sendMessagePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_settings);
        prefSerialList = new ArrayList<>(Arrays.asList(
                getString(R.string.baudrate_key), getString(R.string.databits_key),
                getString(R.string.parity_key), getString(R.string.stopbits_key),
                getString(R.string.flowcontrol_key)
        ));
        listPrefKeys = new String[]{
                getString(R.string.baudrate_key), getString(R.string.databits_key),
                getString(R.string.stopbits_key), getString(R.string.parity_key),
                getString(R.string.flowcontrol_key), getString(R.string.timestamp_format_key),
                getString(R.string.screen_orientation_key), getString(R.string.line_feed_code_key)
        };

        timestampFormatPref = (ListPreference) findPreference(getString(R.string.timestamp_format_key));
        sendMessagePref = (CheckBoxPreference) findPreference(getString(R.string.send_message_visible_key));
        lineFeedCodePref = (ListPreference) findPreference(getString(R.string.line_feed_code_key));
    }

    private void setSummary() {
        ListPreference listPref;
        for (String prefKey : listPrefKeys) {
            listPref = (ListPreference) findPreference(prefKey);
            Log.d(TAG, "Preference: " + prefKey + ", value: " + listPref.getEntry());
            listPref.setSummary(listPref.getEntry());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (prefSerialList.contains(key)) {
            Log.d(TAG, "Restart UsbService");
            Intent intent = new Intent(UsbService.ACTION_SERIAL_CONFIG_CHANGED);
            getActivity().sendBroadcast(intent);
        }

        Preference preference = findPreference(key);
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;

            preference.setSummary(listPref.getEntry());

            if (key.equals(getString(R.string.screen_orientation_key))) {
                Util.setScreenOrientation(listPref.getValue(), getActivity());
            }
        }

        if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkPref = (CheckBoxPreference) preference;

            if (key.equals(getString(R.string.timestamp_visible_key))) {
                timestampFormatPref.setEnabled(checkPref.isChecked());
            }
            if (key.equals(getString(R.string.send_view_visible_key))) {
                sendMessagePref.setEnabled(checkPref.isChecked());
                lineFeedCodePref.setEnabled(checkPref.isChecked());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref = getPreferenceManager().getSharedPreferences();
        pref.registerOnSharedPreferenceChangeListener(this);
        setSummary();

        timestampFormatPref.setEnabled(pref.getBoolean(getString(R.string.timestamp_visible_key), true));
        lineFeedCodePref.setEnabled(pref.getBoolean(getString(R.string.send_view_visible_key), true));
        sendMessagePref.setEnabled(pref.getBoolean(getString(R.string.send_message_visible_key), true));
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
