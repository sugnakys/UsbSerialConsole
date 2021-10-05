package jp.sugnakys.usbserialconsole.presentation.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import jp.sugnakys.usbserialconsole.R

class ConnectionPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_connection_preference, rootKey)

        findPreference<SwitchPreference>(getString(R.string.timestamp_visible_key))
            ?.setOnPreferenceChangeListener { _, newValue ->
                setTimestampEnable(newValue as Boolean)
                true
            }

        findPreference<SwitchPreference>(getString(R.string.send_form_visible_key))
            ?.setOnPreferenceChangeListener { _, newValue ->
                setSendViewEnable(newValue as Boolean)
                true
            }

        setTimestampEnable(
            findPreference<SwitchPreference>(getString(R.string.timestamp_visible_key))
                ?.isChecked ?: true,
        )
        setSendViewEnable(
            findPreference<SwitchPreference>(getString(R.string.send_form_visible_key))
                ?.isChecked ?: true
        )
    }

    private fun setTimestampEnable(isEnable: Boolean) {
        findPreference<androidx.preference.ListPreference>(getString(R.string.timestamp_format_key))
            ?.isEnabled = isEnable
    }

    private fun setSendViewEnable(isEnable: Boolean) {
        findPreference<androidx.preference.ListPreference>(getString(R.string.line_feed_code_send_key))
            ?.isEnabled = isEnable
    }
}