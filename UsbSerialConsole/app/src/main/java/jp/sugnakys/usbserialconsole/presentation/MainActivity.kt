package jp.sugnakys.usbserialconsole.presentation

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.preference.DefaultPreference
import jp.sugnakys.usbserialconsole.usb.UsbPermission
import jp.sugnakys.usbserialconsole.usb.UsbRepository
import jp.sugnakys.usbserialconsole.usb.UsbService
import jp.sugnakys.usbserialconsole.usb.UsbState
import jp.sugnakys.usbserialconsole.util.Util
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var usbRepository: UsbRepository

    @Inject
    lateinit var preference: DefaultPreference

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

    private fun requestConnection() {
        AlertDialog.Builder(this).apply {
            setMessage(getString(R.string.confirm_connect))
            setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                startConnection()
            }
            setNegativeButton(getString(android.R.string.cancel), null)
        }.create().show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usbRepository.sendData.observe(this, {
            if (it.isNotEmpty()) {
                usbService?.write(it)
                usbRepository.clearSendData()
            }
        })

        usbRepository.cts.observe(this, {
            Timber.d("CTS_CHANGE")
            showToast("CTS_CHANGE")
        })

        usbRepository.dsr.observe(this, {
            Timber.d("DSR_CHANGE")
            showToast("DSR_CHANGE")
        }
        )

        usbRepository.state.observe(this, {
            when (it) {
                UsbState.Disconnected -> {
                    showToast(getString(R.string.usb_disconnected))
                    stopConnection()
                }
                UsbState.NoUsb -> showToast(getString(R.string.no_usb))
                UsbState.NotSupported -> showToast(getString(R.string.usb_not_supported))
                else -> {
                }
            }
        })

        usbRepository.permission.observe(this, {
            when (it) {
                UsbPermission.Granted -> {
                    showToast(getString(R.string.usb_permission_granted))
                    requestConnection()
                }
                UsbPermission.NotGranted -> {
                    showToast(getString(R.string.usb_permission_not_granted))
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (preference.sleepMode) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        Util.setScreenOrientation(preference.screenOrientation, this)
    }

    override fun onStart() {
        super.onStart()
        startService()
    }

    override fun onPause() {
        if (usbRepository.isConnect) {
            stopConnection()
            usbRepository.isConnect = false
        }
        unbindService(usbConnection)
        super.onPause()
    }

    private fun startService() {
        val startService = Intent(this, UsbService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startService)
        } else {
            startService(startService)
        }

        bindService()
    }

    private fun bindService() {
        val bindingIntent = Intent(this, UsbService::class.java)
        bindService(bindingIntent, usbConnection, BIND_AUTO_CREATE)
    }

    fun changeConnection() {
        if (usbRepository.isConnect) {
            stopConnection()
        } else {
            startConnection()
        }
    }

    private fun startConnection() {
        usbRepository.isConnect = true
        showToast(getString(R.string.start_connection))
    }

    private fun stopConnection() {
        usbRepository.isConnect = false
        showToast(getString(R.string.stop_connection))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}