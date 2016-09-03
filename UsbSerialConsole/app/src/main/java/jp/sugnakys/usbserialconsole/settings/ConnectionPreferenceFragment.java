package jp.sugnakys.usbserialconsole.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;

import jp.sugnakys.usbserialconsole.R;

public class ConnectionPreferenceFragment extends BasePreferenceFragment {

    private ListPreference timestampFormatPref;
    private SwitchPreference sendMessagePref;
    private ListPreference lineFeedCodePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_connection_preference);

        listPrefKeys = new String[]{
                getString(R.string.line_feed_code_key),
                getString(R.string.timestamp_format_key)};

        timestampFormatPref = (ListPreference) findPreference(getString(R.string.timestamp_format_key));
        sendMessagePref = (SwitchPreference) findPreference(getString(R.string.send_message_visible_key));
        lineFeedCodePref = (ListPreference) findPreference(getString(R.string.line_feed_code_key));
    }

    @Override
    public void onResume() {
        super.onResume();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.connection_title));

        SharedPreferences pref = getPreferenceManager().getSharedPreferences();

        timestampFormatPref.setEnabled(pref.getBoolean(getString(R.string.timestamp_visible_key), true));

        boolean sendViewVisible = pref.getBoolean(getString(R.string.send_view_visible_key), true);
        sendMessagePref.setEnabled(sendViewVisible);
        lineFeedCodePref.setEnabled(sendViewVisible);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);

        if (key.equals(getString(R.string.timestamp_visible_key))) {
            SwitchPreference switchPref = (SwitchPreference) findPreference(key);
            timestampFormatPref.setEnabled(switchPref.isChecked());
        } else if (key.equals(getString(R.string.send_view_visible_key))) {
            SwitchPreference switchPref = (SwitchPreference) findPreference(key);
            sendMessagePref.setEnabled(switchPref.isChecked());
            lineFeedCodePref.setEnabled(switchPref.isChecked());
        }
    }
}
