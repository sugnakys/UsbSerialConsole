package jp.sugnakys.usbserialconsole.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.view.View
import androidx.appcompat.widget.Toolbar
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.util.Util

class DisplayPreferenceFragment : BasePreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.fragment_display_preference)
        listPrefKeys = arrayOf(getString(R.string.screen_orientation_key))
    }

    override fun onResume() {
        super.onResume()
        val toolbar = activity.findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.title = getString(R.string.display_title)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        if (key == getString(R.string.screen_orientation_key)) {
            Util.setScreenOrientation(
                (findPreference(key) as ListPreference).value,
                activity
            )
        }
    }
}