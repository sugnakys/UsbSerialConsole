package jp.sugnakys.usbserialconsole.settings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.preference.ListPreference
import android.preference.PreferenceFragment
import android.view.View
import androidx.appcompat.widget.Toolbar
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.util.Log

open class BasePreferenceFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {
    @JvmField
    var sharedPreference: SharedPreferences? = null

    @JvmField
    var listPrefKeys: Array<String>? = null

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val preference = findPreference(key)
        if (preference is ListPreference) {
            preference.setSummary(preference.entry)
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPreference = preferenceManager.sharedPreferences
        sharedPreference!!.registerOnSharedPreferenceChangeListener(this)
        setSummary()
        val toolbar = activity.findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.setNavigationOnClickListener { fragmentManager.popBackStack() }
    }

    private fun setSummary() {
        if (listPrefKeys == null) {
            return
        }
        var listPref: ListPreference
        for (prefKey in listPrefKeys!!) {
            listPref = findPreference(prefKey) as ListPreference
            Log.d(TAG, "Preference: " + prefKey + ", value: " + listPref.entry)
            listPref.summary = listPref.entry
        }
    }

    override fun onPause() {
        sharedPreference!!.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    companion object {
        private const val TAG = "BasePreferenceFragment"
    }
}