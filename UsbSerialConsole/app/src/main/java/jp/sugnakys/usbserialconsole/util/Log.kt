package jp.sugnakys.usbserialconsole.util

import android.util.Log
import jp.sugnakys.usbserialconsole.BuildConfig

object Log {
    @JvmStatic
    fun e(tag: String?, msg: String?) {
        Log.e(tag, msg!!)
    }

    @JvmStatic
    fun d(tag: String?, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg!!)
        }
    }
}