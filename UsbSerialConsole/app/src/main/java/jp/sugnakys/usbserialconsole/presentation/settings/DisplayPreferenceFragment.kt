package jp.sugnakys.usbserialconsole.presentation.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.util.Util

class DisplayPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.fragment_display_preference)

        findPreference<ListPreference>(getString(R.string.screen_orientation_key))
            ?.setOnPreferenceChangeListener { _, newValue ->
                Util.setScreenOrientation(
                    newValue as String,
                    requireActivity()
                )
                false
            }
    }
}