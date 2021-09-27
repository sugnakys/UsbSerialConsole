package jp.sugnakys.usbserialconsole.usb

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.util.Util
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsbRepository @Inject constructor(
    private val context: Context
) {

    private val _receivedData = MutableLiveData("")
    val receivedData: LiveData<String> = _receivedData

    private var showTimeStamp: Boolean = false
    private var timestampFormat: String = context.getString(R.string.timestamp_format_default)
    private var tmpReceivedData = ""

    private val _isUSBReady = MutableLiveData(false)
    val isUSBReady: LiveData<Boolean> = _isUSBReady

    var isConnect = false

    private val _isSendFormVisibility = MutableLiveData(true)
    val isSendFormVisibility: LiveData<Boolean> = _isSendFormVisibility

    init {
        updateProperties()
    }

    private fun updateProperties() {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        showTimeStamp = pref.getBoolean(
            context.resources.getString(R.string.timestamp_visible_key), true
        )

        timestampFormat = pref.getString(
            context.getString(R.string.timestamp_format_key),
            context.getString(R.string.timestamp_format_default)
        ) ?: context.getString(R.string.timestamp_format_default)
    }

    fun updateReceivedData(data: String) {
        if (showTimeStamp) {
            addReceivedDataWithTime(data)
        } else {
            _receivedData.postValue(_receivedData.value + data)
        }
    }

    fun clearReceivedData() {
        _receivedData.postValue("")
    }

    fun setUsbReady(isReady: Boolean) {
        _isUSBReady.postValue(isReady)
    }

    private fun addReceivedDataWithTime(data: String) {
        val timeStamp = "[" + Util.getCurrentTime(timestampFormat) + "] "
        tmpReceivedData += data
        val separateStr = getLineSeparator(data)
        if (separateStr.isNotEmpty()) {
            val strList = tmpReceivedData.split(separateStr.toRegex())
            tmpReceivedData = ""
            strList.forEachIndexed { index, line ->
                if (strList.size != 1 && index == strList.lastIndex && line.isNotEmpty()) {
                    tmpReceivedData = line
                } else {
                    if (line.isNotEmpty()) {
                        Timber.d("receivedData: $line")
                        _receivedData.postValue(
                            _receivedData.value + timeStamp + line + System.lineSeparator()
                        )
                    }
                }
            }
        }
    }

    private fun getLineSeparator(str: String): String {
        return when {
            str.contains(Util.CR_LF) -> {
                Util.CR_LF
            }
            str.contains(Util.LF) -> {
                Util.LF
            }
            str.contains(Util.CR) -> {
                Util.CR
            }
            else -> {
                ""
            }
        }
    }

}
