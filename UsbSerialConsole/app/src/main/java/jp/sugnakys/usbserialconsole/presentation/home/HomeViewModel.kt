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
import jp.sugnakys.usbserialconsole.device.DeviceRepository
import jp.sugnakys.usbserialconsole.log.LogRepository
import jp.sugnakys.usbserialconsole.preference.DefaultPreference
import jp.sugnakys.usbserialconsole.usb.UsbRepository
import timber.log.Timber

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val deviceRepository: DeviceRepository,
    private val usbRepository: UsbRepository,
    private val preference: DefaultPreference,
    application: Application
) : AndroidViewModel(application) {

    val receivedMessage = usbRepository.receivedData

    val isUSBReady get() = usbRepository.isUSBReady
    val isConnect = usbRepository.isConnect

    fun sendMessage(message: String) {
        if (message.isNotEmpty()) {
            val pattern = Pattern.compile("\n$")
            val matcher = pattern.matcher(message)
            val strResult =
                matcher.replaceAll("") + deviceRepository.getLineFeedCode(preference.lineFeedCodeSend)
            try {
                usbRepository.sendData(strResult)
                Timber.d("SendMessage: $message")
                usbRepository.updateReceivedData(message)
            } catch (e: UnsupportedEncodingException) {
                Timber.e(e.toString())
            }
        }
    }

    fun clearReceivedMessage() = usbRepository.clearReceivedData()

    fun changeConnection(isConnect: Boolean) = usbRepository.changeConnection(isConnect)

    fun writeToFile(file: File, isTimestamp: Boolean): Boolean {
        val text = receivedMessage.value?.joinToString(System.lineSeparator()) {
            val timestamp = if (isTimestamp) {
                val formatTime = SimpleDateFormat(
                    preference.timestampFormat,
                    Locale.US
                ).format(Date(it.timestamp))

                "[$formatTime] "
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

    fun getFileName(date: Date): String {
        val dateText = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.US
        ).format(date)
        return "$dateText.txt"
    }

    fun getLogDir() = logRepository.getLogDir()
}