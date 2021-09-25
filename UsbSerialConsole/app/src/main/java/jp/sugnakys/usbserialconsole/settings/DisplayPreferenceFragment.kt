package jp.sugnakys.usbserialconsole.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.util.Util

@AndroidEntryPoint
class DisplayPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.fragment_display_preference)

        findPreference<androidx.preference.ListPreference>(getString(R.string.screen_orientation_key))?.setOnPreferenceChangeListener { _, newValue ->
            Util.setScreenOrientation(
                newValue as String,
                requireActivity()
            )
            false
        }
    }
}