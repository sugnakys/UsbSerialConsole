package jp.sugnakys.usbserialconsole

import jp.sugnakys.usbserialconsole.util.Util.setScreenOrientation
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import android.preference.PreferenceManager
import jp.sugnakys.usbserialconsole.R

@SuppressLint("Registered")
open class BaseAppCompatActivity : AppCompatActivity() {
    public override fun onResume() {
        super.onResume()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val screenOrientation = pref.getString(
            getString(R.string.screen_orientation_key),
            getString(R.string.screen_orientation_default)
        )
        setScreenOrientation(screenOrientation!!, this)
    }
}