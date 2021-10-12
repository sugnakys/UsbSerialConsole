package jp.sugnakys.usbserialconsole.log

import android.content.Context
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class LogRepository @Inject constructor(
    private val context: Context
){
    companion object {
        private const val LOG_DIR_NAME = "Log"
    }

    fun getLogDir(): File {
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
}