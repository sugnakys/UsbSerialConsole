package jp.sugnakys.usbserialconsole.usb

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.felhr.usbserial.CDCSerialDevice
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface.UsbCTSCallback
import com.felhr.usbserial.UsbSerialInterface.UsbDSRCallback
import com.felhr.usbserial.UsbSerialInterface.UsbReadCallback
import com.felhr.utils.ProtocolBuffer
import dagger.hilt.android.AndroidEntryPoint
import java.nio.charset.Charset
import javax.inject.Inject
import jp.sugnakys.usbserialconsole.device.DeviceRepository
import jp.sugnakys.usbserialconsole.preference.DefaultPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class UsbService : Service() {

    companion object {
        private const val ACTION_USB_PERMISSION = "jp.sugnakys.usbserialconsole.USB_PERMISSION"
    }

    @Inject
    lateinit var usbRepository: UsbRepository

    @Inject
    lateinit var deviceRepository: DeviceRepository

    @Inject
    lateinit var preference: DefaultPreference

    private val binder = UsbBinder()

    private lateinit var usbManager: UsbManager
    private var device: UsbDevice? = null
    private var connection: UsbDeviceConnection? = null
    private var serialPort: UsbSerialDevice? = null

    private var serialPortConnected = false

    private var connectionJob: Job? = null

    private val buffer = ProtocolBuffer(ProtocolBuffer.TEXT)
    private val mCallback = UsbReadCallback { arg ->
        buffer.appendData(arg)
        while(buffer.hasMoreCommands()) {
            usbRepository.updateReceivedData(buffer.nextTextCommand())
        }
    }

    private val ctsCallback = UsbCTSCallback { state ->
        val cts = if (state) CTSState.Raised else CTSState.NotRaised
        usbRepository.changeCTS(cts)
    }

    private val dsrCallback = UsbDSRCallback { state ->
        val dsr = if (state) DSRState.Raised else DSRState.NotRaised
        usbRepository.changeDSR(dsr)
    }

    private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    val extra = intent.extras ?: return
                    val granted = extra.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                    if (granted) {
                        usbRepository.isUSBReady = true
                        usbRepository.changePermission(UsbPermission.Granted)
                    } else {
                        usbRepository.changePermission(UsbPermission.NotGranted)
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    if (!serialPortConnected) {
                        findSerialPortDevice()
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    usbRepository.isUSBReady = false
                    usbRepository.changeState(UsbState.Disconnected)
                    if (serialPortConnected) {
                        serialPort?.close()
                        serialPortConnected = false
                    }
                }
                else -> Timber.e("Unknown action")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        setFilter()

        usbManager = getSystemService(USB_SERVICE) as UsbManager
        findSerialPortDevice()

        usbRepository.isConnect.observeForever { isConnect ->
            if (isConnect) {
                startConnection()
            } else {
                stopConnection()
            }
        }

        usbRepository.settingsEvent.observeForever {
            if (serialPortConnected) {
                serialPort?.close()
                startConnection()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int) = START_NOT_STICKY

    override fun onDestroy() {
        super.onDestroy()
        serialPort?.close()
        unregisterReceiver(usbReceiver)
    }

    fun write(data: String) {
        serialPort?.write(data.toByteArray(Charset.defaultCharset()))
    }

    private fun findSerialPortDevice() {
        val usbDevices = usbManager.deviceList
        if (usbDevices.isEmpty()) {
            usbRepository.changeState(UsbState.NoUsb)
            return
        }

        for ((_, value) in usbDevices) {
            device = value
            if (device != null && UsbSerialDevice.isSupported(device)) {
                requestUserPermission()
                break
            } else {
                connection = null
                device = null
            }
        }
        if (device == null) {
            usbRepository.changeState(UsbState.NoUsb)
        }
    }

    private fun setFilter() {
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        registerReceiver(usbReceiver, filter)
    }

    private fun requestUserPermission() {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }

        val intent =
            PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), flag)
        usbManager.requestPermission(device, intent)
    }

    private fun startConnection() {
        connectionJob = CoroutineScope(Dispatchers.IO).launch {
            connection = usbManager.openDevice(device)
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)
            serialPort?.let {
                if (it.open()) {
                    serialPortConnected = true
                    buffer.setDelimiter(deviceRepository.getLineFeedCode(preference.lineFeedCodeSend))
                    it.setBaudRate(preference.baudrate.toInt())
                    it.setDataBits(preference.databits.toInt())
                    it.setStopBits(preference.stopbits.toInt())
                    it.setParity(preference.parity.toInt())
                    it.setFlowControl(preference.flowcontrol.toInt())

                    it.read(mCallback)
                    it.getCTS(ctsCallback)
                    it.getDSR(dsrCallback)
                    usbRepository.changeState(UsbState.Ready)
                } else {
                    if (serialPort is CDCSerialDevice) {
                        usbRepository.changeState(UsbState.CdcDriverNotWorking)
                    } else {
                        usbRepository.changeState(UsbState.UsbDeviceNotWorking)
                    }
                }

            } ?: run {
                usbRepository.changeState(UsbState.NotSupported)
            }
        }
    }

    private fun stopConnection() {
        connectionJob?.cancel()
        connection?.let {
            it.close()
            connection = null
        }
        if (serialPortConnected) {
            serialPort?.close()
            serialPort = null
            serialPortConnected = false
        }
    }

    inner class UsbBinder : Binder() {
        fun getService(): UsbService = this@UsbService
    }
}