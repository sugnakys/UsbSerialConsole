package jp.sugnakys.usbserialconsole.usb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.sugnakys.usbserialconsole.util.Util
import javax.inject.Inject
import javax.inject.Singleton
import jp.sugnakys.usbserialconsole.data.LogItem
import jp.sugnakys.usbserialconsole.data.LogItemDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class UsbRepository @Inject constructor(
    private val database: LogItemDatabase
) {

    private val dao get() = database.getDao()

    val receivedData = dao.getAllItems()

    private val _sendData = MutableLiveData("")
    val sendData: LiveData<String> = _sendData

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

    fun changeState(state: UsbState) = _state.postValue(state)

    fun changePermission(permission: UsbPermission) = _permission.postValue(permission)

    fun changeCTS() = _cts.postValue(Unit)
    fun changeDSR() = _dsr.postValue(Unit)

    fun sendData(data: String) = _sendData.postValue(data)
    fun clearSendData() = _sendData.postValue("")

    fun updateReceivedData(data: String) {
        val timestamp = System.currentTimeMillis()
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
                        dao.insert(LogItem(timestamp = timestamp, text = line))
                    }
                }
            }
        }
    }

    fun clearReceivedData() =
        CoroutineScope(Dispatchers.IO).launch {
            dao.deleteAll()
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
