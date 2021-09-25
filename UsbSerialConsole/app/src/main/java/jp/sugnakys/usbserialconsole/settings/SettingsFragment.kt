package jp.sugnakys.usbserialconsole.settings

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import jp.sugnakys.usbserialconsole.R

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings_preference, rootKey)

        findPreference<Preference>(getString(R.string.serial_port_key))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_serialPortPreferenceFragment)
            false
        }

        findPreference<Preference>(getString(R.string.display_key))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_displayPreferenceFragment)
            false
        }

        findPreference<Preference>(getString(R.string.connection_key))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_connectionPreferenceFragment)
            false
        }

        findPreference<Preference>(getString(R.string.license_key))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_licenseFragment)
            false
        }
    }
}