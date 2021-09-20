package jp.sugnakys.usbserialconsole

import android.app.Application
import timber.log.Timber

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