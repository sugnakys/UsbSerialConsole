package jp.sugnakys.usbserialconsole.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.SwitchPreference
import androidx.appcompat.widget.Toolbar
import jp.sugnakys.usbserialconsole.R

class ConnectionPreferenceFragment : BasePreferenceFragment() {
    private var timestampFormatPref: ListPreference? = null
    private var lineFeedCodePref: ListPreference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.fragment_connection_preference)
        listPrefKeys = arrayOf(
            getString(R.string.line_feed_code_send_key),
            getString(R.string.timestamp_format_key)
        )
        timestampFormatPref =
            findPreference(getString(R.string.timestamp_format_key)) as ListPreference
        lineFeedCodePref =
            findPreference(getString(R.string.line_feed_code_send_key)) as ListPreference
    }

    override fun onResume() {
        super.onResume()
        val toolbar: Toolbar = activity.findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.connection_title)
        setTimestampEnable(
            sharedPreference!!.getBoolean(
                getString(R.string.timestamp_visible_key),
                true
            )
        )
        setSendViewEnable(
            sharedPreference!!.getBoolean(
                getString(R.string.send_form_visible_key),
                true
            )
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        if (key == getString(R.string.timestamp_visible_key)) {
            setTimestampEnable((findPreference(key) as SwitchPreference).isChecked)
        } else if (key == getString(R.string.send_form_visible_key)) {
            setSendViewEnable((findPreference(key) as SwitchPreference).isChecked)
        }
    }

    private fun setTimestampEnable(isEnable: Boolean) {
        timestampFormatPref!!.isEnabled = isEnable
    }

    private fun setSendViewEnable(isEnable: Boolean) {
        lineFeedCodePref!!.isEnabled = isEnable
    }
}