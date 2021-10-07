package jp.sugnakys.usbserialconsole.presentation.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.usb.UsbRepository
import jp.sugnakys.usbserialconsole.usb.UsbService
import timber.log.Timber

@AndroidEntryPoint
class SerialPortPreferenceFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var usbRepository: UsbRepository

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_serial_port_preference, rootKey)

        findPreference<ListPreference>(getString(R.string.baudrate_key))
            ?.setOnPreferenceChangeListener { _, _ ->
                restartService()
                true
            }

        findPreference<ListPreference>(getString(R.string.databits_key))
            ?.setOnPreferenceChangeListener { _, _ ->
                restartService()
                true
            }

        findPreference<ListPreference>(getString(R.string.parity_key))
            ?.setOnPreferenceChangeListener { _, _ ->
                restartService()
                true
            }

        findPreference<ListPreference>(getString(R.string.stopbits_key))
            ?.setOnPreferenceChangeListener { _, _ ->
                restartService()
                true
            }

        findPreference<ListPreference>(getString(R.string.flowcontrol_key))
            ?.setOnPreferenceChangeListener { _, _ ->
                restartService()
                true
            }
    }

    private fun restartService() {
        Timber.d("Restart UsbService")
        usbRepository.changeSerialSettings()
    }
}