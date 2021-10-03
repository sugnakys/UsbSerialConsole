package jp.sugnakys.usbserialconsole.util

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import java.io.File
import jp.sugnakys.usbserialconsole.R
import timber.log.Timber

class Util {
    companion object {
        private const val LOG_DIR_NAME = "Log"

        const val CR_LF = "\r\n"
        const val LF = "\n"
        const val CR = "\r"

        fun getLogDir(context: Context): File {
            val file = File(context.getExternalFilesDir(null), LOG_DIR_NAME)
            if (!file.exists()) {
                if (file.mkdirs()) {
                    Timber.d("Create Log directory")
                } else {
                    Timber.e("Error: Cannot create Log directory")
                }
            }
            return file
        }

        fun setScreenOrientation(screenOrientation: String, activity: Activity) {
            activity.requestedOrientation = when (screenOrientation) {
                activity.getString(R.string.screen_orientation_portrait_value) -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                activity.getString(R.string.screen_orientation_landscape_value) -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                activity.getString(R.string.screen_orientation_reverse_portrait_value) -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                activity.getString(R.string.screen_orientation_reverse_landscape_value) -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }


    }
}