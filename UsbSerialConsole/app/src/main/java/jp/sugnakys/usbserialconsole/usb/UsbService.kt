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
import dagger.hilt.android.AndroidEntryPoint
import java.nio.charset.Charset
import javax.inject.Inject
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
        private const val ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
        private const val ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"
        const val ACTION_SERIAL_CONFIG_CHANGED =
            "jp.sugnakys.usbserialconsole.SERIAL_CONFIG_CHANGED"
    }

    @Inject
    lateinit var usbRepository: UsbRepository

    @Inject
    lateinit var preference: DefaultPreference

    private val binder = UsbBinder()

    private lateinit var usbManager: UsbManager
    private var device: UsbDevice? = null
    private var connection: UsbDeviceConnection? = null
    private var serialPort: UsbSerialDevice? = null

    private var serialPortConnected = false

    private var connectionJob: Job? = null

    private val mCallback = UsbReadCallback { arg ->
        usbRepository.updateReceivedData(String(arg, Charset.defaultCharset()))

    }

    private val ctsCallback = UsbCTSCallback { usbRepository.changeCTS() }

    private val dsrCallback = UsbDSRCallback { usbRepository.changeDSR() }

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
                ACTION_USB_ATTACHED -> {
                    if (!serialPortConnected) {
                        findSerialPortDevice()
                    }
                }
                ACTION_USB_DETACHED -> {
                    usbRepository.isUSBReady = false
                    usbRepository.changeState(UsbState.Disconnected)
                    if (serialPortConnected) {
                        serialPort?.close()
                        serialPortConnected = false
                    }
                }
                ACTION_SERIAL_CONFIG_CHANGED -> {
                    if (serialPortConnected) {
                        serialPort?.close()
                        startConnection()
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
        filter.addAction(ACTION_USB_DETACHED)
        filter.addAction(ACTION_USB_ATTACHED)
        filter.addAction(ACTION_SERIAL_CONFIG_CHANGED)
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