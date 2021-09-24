package jp.sugnakys.usbserialconsole.util

import android.app.Activity
import android.content.Context
import jp.sugnakys.usbserialconsole.R
import android.content.pm.ActivityInfo
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Util {
    companion object {
        private const val LOG_DIR_NAME = "Log"
        private const val LOG_EXT = ".txt"

        private const val CR_LF = "\r\n"
        private const val LF = "\n"
        private const val CR = "\r"

        fun getCurrentTime(format: String?): String {
            return SimpleDateFormat(format, Locale.US).format(Date(System.currentTimeMillis()))
        }

        fun createLogFileName(): String {
            return SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(Date(System.currentTimeMillis())) + LOG_EXT
        }

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

        fun getLineFeedCd(lineFeedCode: String, context: Context): String {
            return when (lineFeedCode) {
                context.getString(R.string.line_feed_code_cr_value) -> CR
                context.getString(R.string.line_feed_code_lf_value) -> LF
                else -> CR_LF
            }
        }
    }
}