package jp.sugnakys.usbserialconsole.settings

import jp.sugnakys.usbserialconsole.BaseAppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.settings.SettingsPreferenceFragment

class SettingsActivity : BaseAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_main)
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.content_frame, SettingsPreferenceFragment())
        fragmentTransaction.commit()
    }

    override fun onResume() {
        super.onResume()
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }
}