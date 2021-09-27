package jp.sugnakys.usbserialconsole.presentation.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.usb.UsbService
import timber.log.Timber

class SerialPortPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_serial_port_preference, rootKey)

        findPreference<ListPreference>(getString(R.string.baudrate_key))?.setOnPreferenceClickListener {
            restartService()
            false
        }

        findPreference<ListPreference>(getString(R.string.databits_key))?.setOnPreferenceClickListener {
            restartService()
            false
        }

        findPreference<ListPreference>(getString(R.string.parity_key))?.setOnPreferenceClickListener {
            restartService()
            false
        }

        findPreference<ListPreference>(getString(R.string.stopbits_key))?.setOnPreferenceClickListener {
            restartService()
            false
        }

        findPreference<ListPreference>(getString(R.string.flowcontrol_key))?.setOnPreferenceClickListener {
            restartService()
            false
        }
    }

    private fun restartService() {
        Timber.d("Restart UsbService")
        val intent = Intent(UsbService.ACTION_SERIAL_CONFIG_CHANGED)
        activity?.sendBroadcast(intent)
    }
}