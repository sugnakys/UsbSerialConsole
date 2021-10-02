package jp.sugnakys.usbserialconsole.usb

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.preference.DefaultPreference
import jp.sugnakys.usbserialconsole.util.Util
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsbRepository @Inject constructor(
    context: Context,
    private val preference: DefaultPreference
) {
    private val _receivedData = MutableLiveData("")
    val receivedData: LiveData<String> = _receivedData

    private val _sendData = MutableLiveData("")
    val sendData: LiveData<String> = _sendData

    private var showTimeStamp: Boolean = false
    private var timestampFormat: String = context.getString(R.string.timestamp_format_default)
    private var tmpReceivedData = ""

    var isUSBReady = false
    var isConnect = false

    private val _cts = MutableLiveData<Unit>()
    val cts: LiveData<Unit> = _cts

    private val _dsr = MutableLiveData<Unit>()
    val dsr: LiveData<Unit> = _dsr

    private val _state = MutableLiveData<UsbState>(UsbState.Initialized)
    val state: LiveData<UsbState> = _state

    private val _permission = MutableLiveData<UsbPermission>()
    val permission: LiveData<UsbPermission> = _permission

    init {
        updateProperties()
    }

    fun changeState(state: UsbState) {
        _state.postValue(state)
    }

    fun changePermission(permission: UsbPermission) {
        _permission.postValue(permission)
    }

    private fun updateProperties() {
        showTimeStamp = preference.timestampVisibility
        timestampFormat = preference.timestampFormat
    }

    fun changeCTS() {
        _cts.postValue(Unit)
    }

    fun changeDSR() {
        _dsr.postValue(Unit)
    }

    fun sendData(data: String) {
        _sendData.postValue(data)
    }

    fun clearSendData() {
        _sendData.postValue("")
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
            str.contains(Util.CR_LF) -> Util.CR_LF
            str.contains(Util.LF) -> Util.LF
            str.contains(Util.CR) -> Util.CR
            else -> ""
        }
    }
}
