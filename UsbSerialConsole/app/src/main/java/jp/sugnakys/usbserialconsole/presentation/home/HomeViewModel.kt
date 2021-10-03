package jp.sugnakys.usbserialconsole.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.preference.DefaultPreference
import jp.sugnakys.usbserialconsole.usb.UsbRepository
import jp.sugnakys.usbserialconsole.util.Util
import timber.log.Timber

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usbRepository: UsbRepository,
    preference: DefaultPreference,
    application: Application
) : AndroidViewModel(application) {

    val receivedMessage = usbRepository.receivedData

    val isUSBReady get() = usbRepository.isUSBReady
    val isConnect get() = usbRepository.isConnect

    private var lineFeedCode: String = when (preference.lineFeedCodeSend) {
        application.getString(R.string.line_feed_code_cr_value) -> Util.CR
        application.getString(R.string.line_feed_code_lf_value) -> Util.LF
        else -> Util.CR_LF
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

    fun writeToFile(file: File, isTimestamp: Boolean): Boolean {
        val text = receivedMessage.value?.joinToString(System.lineSeparator()) {
            val timestamp = if (isTimestamp) {
                "[${
                    SimpleDateFormat(
                        it.time.format,
                        Locale.US
                    ).format(Date(it.time.unixTime))
                }] "
            } else {
                ""
            }
            timestamp + it.text
        } ?: return false

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
