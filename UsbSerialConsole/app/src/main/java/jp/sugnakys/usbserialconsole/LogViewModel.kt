package jp.sugnakys.usbserialconsole

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset

class LogViewModel: ViewModel() {

    private val _logFile = MutableLiveData<File>()
    val logFile = _logFile

    private val _logText: MutableLiveData<String> = MutableLiveData()
    val logText: LiveData<String> = _logText

    fun setLogfile(file: File) {
        _logFile.postValue(file)

        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = FileInputStream(file)
            val readBytes = ByteArray(fileInputStream.available())
            if (fileInputStream.read(readBytes) != -1) {
                val readString = String(readBytes, Charset.defaultCharset())
                _logText.postValue(readString)
            }
        } catch (e: IOException) {
            Timber.e(e.toString())
        } finally {
            try {
                fileInputStream?.close()
            } catch (e: IOException) {
                Timber.e(e.toString())
            }
        }
    }
}