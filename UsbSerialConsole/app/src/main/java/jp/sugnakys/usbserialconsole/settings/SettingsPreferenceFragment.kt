package jp.sugnakys.usbserialconsole.settings

import android.app.DialogFragment
import android.app.Fragment
import android.os.Bundle
import android.preference.Preference
import android.preference.Preference.OnPreferenceClickListener
import android.view.View
import androidx.appcompat.widget.Toolbar
import jp.sugnakys.usbserialconsole.R

class SettingsPreferenceFragment : BasePreferenceFragment(), OnPreferenceClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.fragment_settings_preference)
        val prefKeys = arrayOf(
            getString(R.string.serial_port_key),
            getString(R.string.display_key),
            getString(R.string.connection_key),
            getString(R.string.license_key)
        )
        for (prefKey in prefKeys) {
            findPreference(prefKey).onPreferenceClickListener = this
        }
    }

    override fun onResume() {
        super.onResume()
        val toolbar = activity.findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.title = getString(R.string.action_settings)
        toolbar.setNavigationOnClickListener { activity.finish() }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        val key = preference.key
        var simpleName: String? = null
        var fragment: Fragment? = null
        if (key == getString(R.string.serial_port_key)) {
            simpleName = SerialPortPreferenceFragment::class.java.simpleName
            fragment = SerialPortPreferenceFragment()
        } else if (key == getString(R.string.display_key)) {
            simpleName = DisplayPreferenceFragment::class.java.simpleName
            fragment = DisplayPreferenceFragment()
        } else if (key == getString(R.string.connection_key)) {
            simpleName = ConnectionPreferenceFragment::class.java.simpleName
            fragment = ConnectionPreferenceFragment()
        } else if (key == getString(R.string.license_key)) {
            val licenseFragment: DialogFragment = LicenseDialogFragment()
            licenseFragment.show(fragmentManager, LicenseDialogFragment::class.java.simpleName)
        }
        if (simpleName != null) {
            fragmentManager
                .beginTransaction()
                .addToBackStack(simpleName)
                .replace(R.id.content_frame, fragment, simpleName)
                .commit()
        }
        return false
    }
}