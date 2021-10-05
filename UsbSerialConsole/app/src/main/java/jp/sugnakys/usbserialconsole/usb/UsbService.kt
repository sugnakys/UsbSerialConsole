package jp.sugnakys.usbserialconsole.usb

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.felhr.usbserial.CDCSerialDevice
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface.UsbCTSCallback
import com.felhr.usbserial.UsbSerialInterface.UsbDSRCallback
import com.felhr.usbserial.UsbSerialInterface.UsbReadCallback
import dagger.hilt.android.AndroidEntryPoint
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.preference.DefaultPreference
import timber.log.Timber
import java.nio.charset.Charset
import javax.inject.Inject

@AndroidEntryPoint
class UsbService : Service() {

    companion object {
        private const val ACTION_USB_PERMISSION = "jp.sugnakys.usbserialconsole.USB_PERMISSION"
        private const val ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
        private const val ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"
        const val ACTION_SERIAL_CONFIG_CHANGED = "jp.sugnakys.usbserialconsole.SERIAL_CONFIG_CHANGED"
    }

    @Inject
    lateinit var usbRepository: UsbRepository

    @Inject
    lateinit var preference: DefaultPreference

    private val binder = UsbBinder()

    private lateinit var context: Context
    private lateinit var usbManager: UsbManager
    private var device: UsbDevice? = null
    private var connection: UsbDeviceConnection? = null
    private var serialPort: UsbSerialDevice? = null

    private var serialPortConnected = false

    private val mCallback = UsbReadCallback { arg ->
        if (usbRepository.isConnect) {
            usbRepository.updateReceivedData(String(arg, Charset.defaultCharset()))
        }
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
                        connection = usbManager.openDevice(device)
                        ConnectionThread().start()
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
                    if (serialPortConnected) { serialPort?.close() }
                    serialPortConnected = false
                }
                ACTION_SERIAL_CONFIG_CHANGED -> {
                    if (serialPortConnected) {
                        Timber.d("Restart Connection")
                        serialPort?.close()
                        connection = usbManager.openDevice(device)
                        ConnectionThread().start()
                    }
                }
                else -> Timber.e("Unknown action")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        context = this
        serialPortConnected = false

        setFilter()

        usbManager = getSystemService(USB_SERVICE) as UsbManager
        findSerialPortDevice()
    }

    override fun onBind(intent: Intent): IBinder  = binder

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                ""
            }
        val notification = NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .apply {
                setSmallIcon(R.drawable.swap_horizontal)
                setContentTitle(getString(R.string.app_name))
                setContentText(getString(R.string.service_starting_up))
            }.build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "USB_SERIAL_CONSOLE_SERVICE"
        val channelName = getString(R.string.usb_connection_service)

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )

        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)

        return channelId
    }

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

    inner class UsbBinder : Binder() {
        fun getService(): UsbService = this@UsbService
    }

    private inner class ConnectionThread : Thread() {
        override fun run() {
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
}