package jp.sugnakys.usbserialconsole

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import jp.sugnakys.usbserialconsole.util.Constants
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset

class LogViewFragment : Fragment() {

    val args: LogViewFragmentArgs by navArgs()

    private var logFile: File? = null
    private lateinit var toolbar: Toolbar
    private lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView = view.findViewById(R.id.logView)

        toolbar = view.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_log_view)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_send_to -> {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.setDataAndType(Uri.fromFile(logFile), "text/plain")
                    startActivity(intent)
                    true
                }
                else -> {
                    Timber.e("Unknown id")
                    false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        logFile = Uri.parse(args.uriString).toFile()
        logFile?.let {
            toolbar.title = it.name
            setLogText(it)
        }
    }

    private fun setLogText(file: File) {
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = FileInputStream(file)
            val readBytes = ByteArray(fileInputStream.available())
            if (fileInputStream.read(readBytes) != -1) {
                val readString = String(readBytes, Charset.forName(Constants.CHARSET))
                textView.text = readString
            }
        } catch (e: IOException) {
            Timber.e(e.toString())
        } finally {
            try {
                fileInputStream?.close()
            } catch (e: IOException) {
                Timber.e(e.toString())
            }
        }
    }
}