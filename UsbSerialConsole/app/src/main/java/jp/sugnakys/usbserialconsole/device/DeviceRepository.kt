package jp.sugnakys.usbserialconsole.device

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import javax.inject.Inject
import javax.inject.Singleton
import jp.sugnakys.usbserialconsole.R

@Singleton
class DeviceRepository @Inject constructor(
    private val context: Context
) {

    companion object {
        const val CR_LF = "\r\n"
        const val LF = "\n"
        const val CR = "\r"
    }

    fun getLineFeedCode(lineFeedCode: String) = when (lineFeedCode) {
        context.getString(R.string.line_feed_code_cr_value) -> CR
        context.getString(R.string.line_feed_code_lf_value) -> LF
        else -> CR_LF
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

    fun setSleepMode(isSleepMode: Boolean, activity: Activity) {
        if (isSleepMode) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}