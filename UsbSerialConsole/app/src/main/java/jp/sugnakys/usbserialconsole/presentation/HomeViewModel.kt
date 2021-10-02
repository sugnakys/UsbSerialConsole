package jp.sugnakys.usbserialconsole.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.sugnakys.usbserialconsole.preference.DefaultPreference
import jp.sugnakys.usbserialconsole.usb.UsbRepository
import jp.sugnakys.usbserialconsole.util.Util.Companion.getLineFeedCd
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usbRepository: UsbRepository,
    preference: DefaultPreference,
    application: Application
) : AndroidViewModel(application) {

    val receivedMessage = usbRepository.receivedData

    val isUSBReady get() = usbRepository.isUSBReady
    val isConnect get() = usbRepository.isConnect

    private var lineFeedCode: String? = null

    init {
        lineFeedCode = getLineFeedCd(
            preference.lineFeedCodeSend,
            application
        )
    }

    fun sendMessage(message: String) {
        if (message.isNotEmpty()) {
            val pattern = Pattern.compile("\n$")
            val matcher = pattern.matcher(message)
            val strResult = matcher.replaceAll("") + lineFeedCode
            try {
                usbRepository.sendData(strResult)
                Timber.d("SendMessage: $message")
                usbRepository.updateReceivedData(message)
            } catch (e: UnsupportedEncodingException) {
                Timber.e(e.toString())
            }
        }
    }

    fun clearReceivedMessage() {
        usbRepository.clearReceivedData()
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
