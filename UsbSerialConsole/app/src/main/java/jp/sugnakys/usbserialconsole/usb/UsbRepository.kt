package jp.sugnakys.usbserialconsole.usb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton
import jp.sugnakys.usbserialconsole.data.LogItem
import jp.sugnakys.usbserialconsole.data.LogItemDatabase
import jp.sugnakys.usbserialconsole.device.DeviceRepository
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

    private val _isConnect = MutableLiveData(false)
    val isConnect: LiveData<Boolean> = _isConnect

    private val _cts = MutableLiveData<Unit>()
    val cts: LiveData<Unit> = _cts

    private val _dsr = MutableLiveData<Unit>()
    val dsr: LiveData<Unit> = _dsr

    private val _state = MutableLiveData<UsbState>(UsbState.Initialized)
    val state: LiveData<UsbState> = _state

    private val _permission = MutableLiveData<UsbPermission>()
    val permission: LiveData<UsbPermission> = _permission

    private val _settingsEvent = MutableLiveData<Unit>()
    val settingsEvent: LiveData<Unit> = _settingsEvent

    fun changeState(state: UsbState) = _state.postValue(state)

    fun changePermission(permission: UsbPermission) = _permission.postValue(permission)

    fun changeSerialSettings() = _settingsEvent.postValue(Unit)

    fun changeCTS() = _cts.postValue(Unit)
    fun changeDSR() = _dsr.postValue(Unit)

    fun changeConnection(isConnect: Boolean) = _isConnect.postValue(isConnect)

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
            str.contains(DeviceRepository.CR_LF) -> DeviceRepository.CR_LF
            str.contains(DeviceRepository.LF) -> DeviceRepository.LF
            str.contains(DeviceRepository.CR) -> DeviceRepository.CR
            else -> ""
        }
    }
}
