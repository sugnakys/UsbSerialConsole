package jp.sugnakys.usbserialconsole.presentation

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.device.DeviceRepository
import jp.sugnakys.usbserialconsole.preference.DefaultPreference
import jp.sugnakys.usbserialconsole.usb.UsbPermission
import jp.sugnakys.usbserialconsole.usb.UsbRepository
import jp.sugnakys.usbserialconsole.usb.UsbService
import jp.sugnakys.usbserialconsole.usb.UsbState

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var deviceRepository: DeviceRepository

    @Inject
    lateinit var usbRepository: UsbRepository

    @Inject
    lateinit var preference: DefaultPreference

    private var usbService: UsbService? = null

    private val usbConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as UsbService.UsbBinder
            usbService = binder.getService()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            usbService = null
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

        deviceRepository.setSleepMode(preference.sleepMode, this)
        deviceRepository.setScreenOrientation(preference.screenOrientation, this)

        setContentView(R.layout.activity_main)

        val navHost = supportFragmentManager.findFragmentById(R.id.main_fragment_host)
        val navController = navHost!!.findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        usbRepository.sendData.observe(this, {
            if (it.isNotEmpty()) {
                usbService?.write(it)
                usbRepository.clearSendData()
            }
        })

        usbRepository.cts.observe(this, {
            showToast("CTS change state: $it")
        })

        usbRepository.dsr.observe(this, {
            showToast("DSR change state: $it")
        })

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

        startService()
    }

    override fun onSupportNavigateUp() =
        findNavController(R.id.main_fragment_host).navigateUp()

    override fun onDestroy() {
        super.onDestroy()
        if (usbRepository.isConnect.value == true) {
            stopConnection()
        }
        unbindService(usbConnection)
    }

    private fun startService() {
        startService(Intent(this, UsbService::class.java))

        bindService(
            Intent(this, UsbService::class.java),
            usbConnection,
            BIND_AUTO_CREATE
        )

    }

    private fun startConnection() {
        usbRepository.changeConnection(true)
        showToast(getString(R.string.start_connection))
    }

    private fun stopConnection() {
        usbRepository.changeConnection(false)
        showToast(getString(R.string.stop_connection))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}