package jp.sugnakys.usbserialconsole

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import jp.sugnakys.usbserialconsole.util.Util
import timber.log.Timber
import java.io.File

class LogListFragment : Fragment() {
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.action_log_list)

        listView = view.findViewById(R.id.listView)
        listView.setOnItemClickListener { adapterView, _, position, _ ->
            val listView = adapterView as ListView
            val selectedItem = listView.getItemAtPosition(position) as String
            val targetFile = File(Util.getLogDir(requireContext()), selectedItem)
            val action = LogListFragmentDirections
                .actionLogListFragmentToLogViewFragment(
                    targetFile.toUri().toString()
                )
            findNavController().navigate(action)
        }
        listView.setOnItemLongClickListener { adapterView, _, position, _ ->
            val listView = adapterView as ListView
            val selectedItem = listView.getItemAtPosition(position) as String
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_log_file_title))
                .setMessage(
                    """
                    |${getString(R.string.delete_log_file_text)}
                    |${getString(R.string.file_name)}: $selectedItem
                """.trimMargin()
                )
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val targetFile = File(Util.getLogDir(requireContext()), selectedItem)
                    if (deleteLogFile(targetFile)) {
                        updateList()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        updateList()
    }

    private fun deleteLogFile(file: File): Boolean {
        Timber.d("Delete file path: ${file.name}")
        return file.delete()
    }

    private val fileNameList: Array<String?>?
        get() {
            val file = Util.getLogDir(requireContext()).listFiles()
            if (file == null) {
                Timber.w("File not found")
                return null
            }
            val fileName = arrayOfNulls<String>(file.size)
            for (i in file.indices) {
                fileName[i] = file[i].name
            }
            return fileName
        }

    private fun updateList() {
        val files = fileNameList
        if (files == null) {
            Timber.w("File not found")
            return
        }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            files
        )
        listView.adapter = adapter
    }
}