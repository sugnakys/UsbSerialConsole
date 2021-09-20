package jp.sugnakys.usbserialconsole

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import jp.sugnakys.usbserialconsole.UsbService.UsbBinder
import jp.sugnakys.usbserialconsole.util.Constants
import jp.sugnakys.usbserialconsole.util.Util
import jp.sugnakys.usbserialconsole.util.Util.currentDateForFile
import jp.sugnakys.usbserialconsole.util.Util.getLogDir
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private var usbService: UsbService? = null
    private val usbConnection = object : ServiceConnection {
        override fun onServiceConnected(arg0: ComponentName, arg1: IBinder) {
            usbService = (arg1 as UsbBinder).service
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            usbService = null
        }
    }

    private var mOptionMenu: Menu? = null
    //private var mHandler: MyHandler? = null
    private var timestampFormat: String? = null
    private var lineFeedCode: String? = null
    private var tmpReceivedData = ""
    private var showTimeStamp = false
    private var isUSBReady = false
    private var isConnect = false

    private val mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbService.ACTION_USB_PERMISSION_GRANTED -> {
                    Toast.makeText(
                        context,
                        getString(R.string.usb_permission_granted),
                        Toast.LENGTH_SHORT
                    ).show()
                    isUSBReady = true
                    updateOptionsMenu()
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
                    isUSBReady = false
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
        alertDialog.setPositiveButton(getString(android.R.string.ok)) { dialogInterface, i -> startConnection() }
        alertDialog.setNegativeButton(getString(android.R.string.cancel), null)
        alertDialog.create().show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.AppTheme_NoActionBar)
        //mHandler = MyHandler(this)
        setContentView(R.layout.activity_main)
    }

//    private fun setDefaultColor() {
//        val pref = PreferenceManager.getDefaultSharedPreferences(this)
//        val editor = pref.edit()
//        if (!pref.contains(getString(R.string.color_console_background_key))) {
//            var defaultBackgroundColor = Color.TRANSPARENT
//            val background = mainLayout!!.background
//            if (background is ColorDrawable) {
//                defaultBackgroundColor = background.color
//            }
//            editor.putInt(getString(R.string.color_console_background_key), defaultBackgroundColor)
//            editor.apply()
//            d(TAG, "Default background color: " + String.format("#%08X", defaultBackgroundColor))
//        }
//        if (!pref.contains(getString(R.string.color_console_text_key))) {
//            val defaultTextColor = receivedMsgView!!.textColors.defaultColor
//            editor.putInt(getString(R.string.color_console_text_key), defaultTextColor)
//            editor.apply()
//            d(TAG, "Default text color: " + String.format("#%08X", defaultTextColor))
//        }
//    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putString(RECEIVED_TEXT_VIEW_STR, receivedMsgView!!.text.toString())
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        receivedMsgView!!.text = savedInstanceState.getString(RECEIVED_TEXT_VIEW_STR)
//    }

    override fun onResume() {
        super.onResume()
//        setDefaultColor()
//        val pref = PreferenceManager.getDefaultSharedPreferences(this)
//        showTimeStamp = pref.getBoolean(
//            resources.getString(R.string.timestamp_visible_key), true
//        )
//        timestampFormat = pref.getString(
//            getString(R.string.timestamp_format_key),
//            getString(R.string.timestamp_format_default)
//        )
//        lineFeedCode = getLineFeedCd(
//            pref.getString(
//                getString(R.string.line_feed_code_send_key),
//                getString(R.string.line_feed_code_cr_lf_value)
//            )!!,
//            this
//        )
//        if (pref.getBoolean(getString(R.string.send_form_visible_key), true)) {
//            sendViewLayout!!.visibility = View.VISIBLE
//        } else {
//            sendViewLayout!!.visibility = View.GONE
//        }
//        if (pref.getBoolean(getString(R.string.sleep_mode_key), false)) {
//            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        } else {
//            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        }
//        val backgroundColor =
//            pref.getInt(getString(R.string.color_console_background_key), Color.WHITE)
//        d(TAG, "Background color: " + String.format("#%08X", backgroundColor))
//        mainLayout!!.setBackgroundColor(backgroundColor)
//        val textColor = pref.getInt(getString(R.string.color_console_text_key), Color.BLACK)
//        d(TAG, "Text color: " + String.format("#%08X", textColor))
//        receivedMsgView!!.setTextColor(textColor)
//        sendMsgView!!.setTextColor(textColor)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val screenOrientation = pref.getString(
            getString(R.string.screen_orientation_key),
            getString(R.string.screen_orientation_default)
        )
        Util.setScreenOrientation(screenOrientation!!, this)

        setFilters()
        startService(UsbService::class.java, usbConnection)
        updateOptionsMenu()
    }

    public override fun onDestroy() {
        if (isConnect) {
            stopConnection()
        }
        unregisterReceiver(mUsbReceiver)
        unbindService(usbConnection)
        super.onDestroy()
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            val alertDialog = AlertDialog.Builder(this@MainActivity)
//            alertDialog.setMessage(getString(R.string.confirm_finish_text))
//            alertDialog.setPositiveButton(getString(android.R.string.ok)) { dialogInterface, i -> finish() }
//            alertDialog.setNegativeButton(getString(android.R.string.cancel), null)
//            alertDialog.create().show()
//            return true
//        }
//        return false
//    }

    private fun startService(service: Class<*>, serviceConnection: ServiceConnection) {
        if (!UsbService.SERVICE_CONNECTED) {
            val startService = Intent(this, service)
            startService(startService)
        }
        val bindingIntent = Intent(this, service)
        bindService(bindingIntent, serviceConnection, BIND_AUTO_CREATE)
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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mOptionMenu = menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun updateOptionsMenu() {
        if (mOptionMenu != null) {
            onPrepareOptionsMenu(mOptionMenu!!)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.action_connect)
        item.isEnabled = isUSBReady
        if (isConnect) {
            item.title = getString(R.string.action_disconnect)
        } else {
            item.title = getString(R.string.action_connect)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun writeToFile(data: String) {
        val fileName = currentDateForFile + Constants.LOG_EXT
        val dirName = getLogDir(applicationContext)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(File(dirName, fileName))
            fos.write(data.toByteArray(Charset.forName(Constants.CHARSET)))
            Timber.d( "Save: $fileName")
            Toast.makeText(
                this, getString(R.string.action_save_log)
                        + " : " + fileName, Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            Timber.e( e.toString())
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                Timber.e(e.toString())
            }
        }
    }

    private fun startConnection() {
        //usbService!!.setHandler(mHandler)
        isConnect = true
        Toast.makeText(
            applicationContext,
            getString(R.string.start_connection), Toast.LENGTH_SHORT
        ).show()
        updateOptionsMenu()
    }

    private fun stopConnection() {
        //usbService!!.setHandler(null)
        isConnect = false
        Toast.makeText(
            applicationContext,
            getString(R.string.stop_connection), Toast.LENGTH_SHORT
        ).show()
        updateOptionsMenu()
    }

//    private fun addReceivedData(data: String) {
//        if (showTimeStamp) {
//            addReceivedDataWithTime(data)
//        } else {
//            addTextView(data)
//        }
//    }

//    private fun addTextView(data: String) {
//        receivedMsgView!!.append(data)
//        scrollView!!.scrollTo(0, receivedMsgView!!.bottom)
//    }

//    private fun addReceivedDataWithTime(data: String) {
//        val timeStamp = "[" + getCurrentTime(timestampFormat) + "] "
//        tmpReceivedData += data
//        val separateStr = getLineSeparater(tmpReceivedData)
//        if (!separateStr.isEmpty()) {
//            val strArray = tmpReceivedData.split(separateStr.toRegex()).toTypedArray()
//            tmpReceivedData = ""
//            for (i in strArray.indices) {
//                if (strArray.size != 1 && i == strArray.size - 1 && !strArray[i].isEmpty()) {
//                    tmpReceivedData = strArray[i]
//                } else {
//                    addTextView(timeStamp + strArray[i] + System.lineSeparator())
//                }
//            }
//        }
//    }

//    private fun getLineSeparater(str: String): String {
//        return if (str.contains(Constants.CR_LF)) {
//            Constants.CR_LF
//        } else if (str.contains(Constants.LF)) {
//            Constants.LF
//        } else if (str.contains(Constants.CR)) {
//            Constants.CR
//        } else {
//            ""
//        }
//    }
//
//    private class MyHandler(activity: MainActivity) : Handler() {
//        private val mActivity: WeakReference<MainActivity>
//        override fun handleMessage(msg: Message) {
//            when (msg.what) {
//                UsbService.MESSAGE_FROM_SERIAL_PORT -> {
//                    val data = msg.obj as String
//                    if (data != null) {
//                        mActivity.get()!!.addReceivedData(data)
//                    }
//                }
//                UsbService.CTS_CHANGE -> {
//                    d(TAG, "CTS_CHANGE")
//                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show()
//                }
//                UsbService.DSR_CHANGE -> {
//                    d(TAG, "DSR_CHANGE")
//                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show()
//                }
//                else -> e(TAG, "Unknown message")
//            }
//        }
//
//        init {
//            mActivity = WeakReference(activity)
//        }
//    }

//    companion object {
//        private const val RECEIVED_TEXT_VIEW_STR = "RECEIVED_TEXT_VIEW_STR"
//    }
}