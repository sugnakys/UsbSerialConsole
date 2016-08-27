package jp.co.sugnakys.usbserialconsole;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(android.R.id.content, new SettingsPreferenceFragment());
        fragmentTransaction.commit();
    }
}
