package jp.sugnakys.usbserialconsole.presentation

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.usb.UsbRepository
import jp.sugnakys.usbserialconsole.usb.UsbService
import jp.sugnakys.usbserialconsole.util.Util
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var usbRepository: UsbRepository

    private var usbService: UsbService? = null
    private var bound = false

    private val usbConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as UsbService.UsbBinder
            usbService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    private var mHandler: MyHandler? = null

    private val mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbService.ACTION_USB_PERMISSION_GRANTED -> {
                    Toast.makeText(
                        context,
                        getString(R.string.usb_permission_granted),
                        Toast.LENGTH_SHORT
                    ).show()
                    usbRepository.setUsbReady(true)
                    requestConnection()
                }
                UsbService.ACTION_USB_PERMISSION_NOT_GRANTED -> Toast.makeText(
                    context,
                    getString(R.string.usb_permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
                UsbService.ACTION_NO_USB -> Toast.makeText(
                    context,
                    getString(R.string.no_usb),
                    Toast.LENGTH_SHORT
                ).show()
                UsbService.ACTION_USB_DISCONNECTED -> {
                    Toast.makeText(
                        context,
                        getString(R.string.usb_disconnected),
                        Toast.LENGTH_SHORT
                    ).show()
                    usbRepository.setUsbReady(false)
                    stopConnection()
                }
                UsbService.ACTION_USB_NOT_SUPPORTED -> Toast.makeText(
                    context, getString(R.string.usb_not_supported),
                    Toast.LENGTH_SHORT
                ).show()
                else -> Timber.e("Unknown action")
            }
        }
    }

    private fun requestConnection() {
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        alertDialog.setMessage(getString(R.string.confirm_connect))
        alertDialog.setPositiveButton(getString(android.R.string.ok)) { _, _ -> startConnection() }
        alertDialog.setNegativeButton(getString(android.R.string.cancel), null)
        alertDialog.create().show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mHandler = MyHandler(this)
    }

    override fun onResume() {
        super.onResume()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if (pref.getBoolean(getString(R.string.sleep_mode_key), false)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        val screenOrientation = pref.getString(
            getString(R.string.screen_orientation_key),
            getString(R.string.screen_orientation_default)
        )
        Util.setScreenOrientation(screenOrientation!!, this)

        setFilters()
    }

    override fun onStart() {
        super.onStart()
        startService()
    }

    override fun onPause() {
        if (usbRepository.isConnect) {
            stopConnection()
        }
        unregisterReceiver(mUsbReceiver)
        unbindService(usbConnection)
        super.onPause()
    }

    private fun startService() {
        if (!UsbService.SERVICE_CONNECTED) {
            val startService = Intent(this, UsbService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startService)
            } else {
                startService(startService)
            }
        }
        bindService()
    }

    private fun bindService() {
        val bindingIntent = Intent(this, UsbService::class.java)
        bindService(bindingIntent, usbConnection, BIND_AUTO_CREATE)
    }

    private fun setFilters() {
        val filter = IntentFilter()
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED)
        filter.addAction(UsbService.ACTION_NO_USB)
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED)
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED)
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED)
        registerReceiver(mUsbReceiver, filter)
    }

    fun changeConnection() {
        if (usbRepository.isConnect) {
            stopConnection()
        } else {
            startConnection()
        }
    }

    private fun startConnection() {
        usbService?.setHandler(mHandler)
        usbRepository.isConnect = true
        Toast.makeText(
            applicationContext,
            getString(R.string.start_connection), Toast.LENGTH_SHORT
        ).show()
    }

    private fun stopConnection() {
        usbService?.setHandler(null)
        usbRepository.isConnect = false
        Toast.makeText(
            applicationContext,
            getString(R.string.stop_connection), Toast.LENGTH_SHORT
        ).show()
    }

    private fun addReceivedData(data: String) {
        usbRepository.updateReceivedData(data)
    }

    private class MyHandler(activity: MainActivity) : Handler(Looper.getMainLooper()) {
        private val mActivity: WeakReference<MainActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                UsbService.MESSAGE_FROM_SERIAL_PORT -> {
                    val data = msg.obj as String
                    mActivity.get()?.addReceivedData(data)
                }
                UsbService.CTS_CHANGE -> {
                    Timber.d("CTS_CHANGE")
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show()
                }
                UsbService.DSR_CHANGE -> {
                    Timber.d("DSR_CHANGE")
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show()
                }
                else -> Timber.e("Unknown message")
            }
        }

    }
}