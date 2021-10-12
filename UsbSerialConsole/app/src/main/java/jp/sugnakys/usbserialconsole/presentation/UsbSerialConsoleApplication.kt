package jp.sugnakys.usbserialconsole.presentation

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import jp.sugnakys.usbserialconsole.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class UsbSerialConsoleApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}