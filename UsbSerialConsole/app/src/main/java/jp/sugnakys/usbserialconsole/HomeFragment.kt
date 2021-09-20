package jp.sugnakys.usbserialconsole

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import timber.log.Timber

class HomeFragment : Fragment() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var sendBtn: Button
    private lateinit var sendMsgView: EditText
    private lateinit var sendViewLayout: LinearLayout
    private lateinit var receivedMsgView: TextView
    private lateinit var scrollView: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainLayout = view.findViewById(R.id.mainLayout)
        receivedMsgView = view.findViewById(R.id.receivedMsgView)
        scrollView = view.findViewById(R.id.scrollView)
        sendBtn = view.findViewById(R.id.sendBtn)
        sendMsgView = view.findViewById(R.id.sendMsgView)
        sendViewLayout = view.findViewById(R.id.sendViewLayout)

        sendBtn.setOnClickListener {
            Timber.d("Send button clicked")
            var message = sendMsgView!!.text.toString()
            if (!message.isEmpty()) {
                message += System.lineSeparator()
                sendMessage(message)
                sendMsgView.setText("")
            }
            Timber.d("Unknown view")
        }

        sendMsgView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                sendBtn.isEnabled = s.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_connect -> {
                    Timber.d("Connect clicked")
//                if (isConnect) {
//                    stopConnection()
//                } else {
//                    startConnection()
//                }
                    true
                }
                R.id.action_clear_log -> {
                    Timber.d("Clear log clicked")
//                receivedMsgView!!.text = ""
                    true
                }
                R.id.action_save_log -> {
                    Timber.d("Save log clicked")
//                writeToFile(receivedMsgView!!.text.toString())
                    true
                }
                R.id.action_settings -> {
                    Timber.d("Settings clicked")
                    findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
                    true
                }
                R.id.action_log_list -> {
                    Timber.d("Log list clicked")
                    findNavController().navigate(R.id.action_homeFragment_to_logListFragment)
                    true
                }
                else -> {
                    Timber.e("Unknown id")
                    false
                }
            }
        }

    }

    private fun sendMessage(msg: String) {
//        val pattern = Pattern.compile("\n$")
//        val matcher = pattern.matcher(msg)
//        val strResult = matcher.replaceAll("") + lineFeedCode
//        try {
//            usbService!!.write(strResult.toByteArray(Charset.forName(Constants.CHARSET)))
//            Timber.d("SendMessage: $msg")
//            addReceivedData(msg)
//        } catch (e: UnsupportedEncodingException) {
//            Timber.e(e.toString())
//        }
    }
}