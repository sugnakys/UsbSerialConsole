package jp.sugnakys.usbserialconsole.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.System.lineSeparator
import java.nio.charset.Charset

class HomeViewModel : ViewModel() {

    private val isConnection = false

    private val _receivedMessage = MutableLiveData<String>()
    val receivedMessage: LiveData<String> = _receivedMessage

    fun sendMessage(message: String) {
        if (message.isNotEmpty()) {
            val sendMessage = message + lineSeparator()

//        val pattern = Pattern.compile("\n$")
//        val matcher = pattern.matcher(msg)
//        val strResult = matcher.replaceAll("") + lineFeedCode
//        try {
//            usbService!!.write(strResult.toByteArray(Charset.forName(Constants.CHARSET)))
//            Timber.d("SendMessage: $msg")
//            addReceivedData(msg)
//        } catch (e: UnsupportedEncodingException) {
//            Timber.e(e.toString())
//        }

        }
    }

    fun clearReceivedMessage() {
        _receivedMessage.postValue("")
    }

    fun changeConnection() {
        if (isConnection) {
            stopConnection()
        } else {
            startConnection()
        }
    }

    private fun startConnection() {
        //TODO
    }

    private fun stopConnection() {
        //TODO
    }

    fun writeToFile(file: File, text: String): Boolean {
        var result = false
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            fos.write(text.toByteArray(Charset.defaultCharset()))
            result = true
        } catch (e: IOException) {
            Timber.e(e.toString())
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                Timber.e(e.toString())
            }
        }
        return result
    }
}
