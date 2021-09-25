package jp.sugnakys.usbserialconsole.presentation

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import jp.sugnakys.usbserialconsole.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class UsbSerialConsoleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        configureTimber()
    }

    private fun configureTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}