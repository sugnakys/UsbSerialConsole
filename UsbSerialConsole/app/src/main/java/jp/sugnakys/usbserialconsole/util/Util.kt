package jp.sugnakys.usbserialconsole.util

import android.app.Activity
import android.content.Context
import jp.sugnakys.usbserialconsole.R
import android.content.pm.ActivityInfo
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Util {
    private const val TAG = "Util"
    @JvmStatic
    fun getCurrentTime(format: String?): String {
        return SimpleDateFormat(format, Locale.US).format(Date(System.currentTimeMillis()))
    }

    @JvmStatic
    val currentDateForFile: String
        get() = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.US
        ).format(Date(System.currentTimeMillis()))

    @JvmStatic
    fun getLogDir(context: Context): File {
        val file = File(context.getExternalFilesDir(null), Constants.LOG_DIR_NAME)
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(TAG, "Error: Cannot create Log directory")
            } else {
                Log.d(TAG, "Create Log directory")
            }
        }
        return file
    }

    @JvmStatic
    fun setScreenOrientation(screenOrientation: String, activity: Activity) {
        if (screenOrientation ==
            activity.getString(R.string.screen_orientation_portrait_value)
        ) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (screenOrientation ==
            activity.getString(R.string.screen_orientation_landscape_value)
        ) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else if (screenOrientation ==
            activity.getString(R.string.screen_orientation_reverse_portrait_value)
        ) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        } else if (screenOrientation ==
            activity.getString(R.string.screen_orientation_reverse_landscape_value)
        ) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    @JvmStatic
    fun getLineFeedCd(lineFeedCode: String, context: Context): String {
        return if (lineFeedCode == context.getString(R.string.line_feed_code_cr_value)) {
            Log.d(TAG, "Line feed code: CR")
            Constants.CR
        } else if (lineFeedCode == context.getString(R.string.line_feed_code_lf_value)) {
            Log.d(TAG, "Line feed code: LF")
            Constants.LF
        } else {
            Log.d(TAG, "Line feed code: CRLF")
            Constants.CR_LF
        }
    }
}