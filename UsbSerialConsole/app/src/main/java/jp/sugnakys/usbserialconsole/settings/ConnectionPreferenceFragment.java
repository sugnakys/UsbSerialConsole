package jp.sugnakys.usbserialconsole.settings;

import android.os.Bundle;

import jp.sugnakys.usbserialconsole.R;

public class ConnectionPreferenceFragment extends BasePreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_connection_preference);

        listPrefKeys = new String[]{
                getString(R.string.line_feed_code_key),
                getString(R.string.timestamp_format_key)};
    }
}
