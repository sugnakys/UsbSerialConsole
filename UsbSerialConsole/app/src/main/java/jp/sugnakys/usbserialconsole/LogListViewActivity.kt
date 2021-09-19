package jp.sugnakys.usbserialconsole

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import jp.sugnakys.usbserialconsole.LogViewActivity
import jp.sugnakys.usbserialconsole.util.Constants
import jp.sugnakys.usbserialconsole.util.Util.getLogDir
import java.io.File

class LogListViewActivity : BaseAppCompatActivity(), OnItemClickListener, OnItemLongClickListener {
    private var listView: ListView? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_list_view_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.title = getString(R.string.action_log_list)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            toolbar.setNavigationOnClickListener { finish() }
        }
        listView = findViewById<View>(R.id.listView) as ListView
        listView!!.onItemClickListener = this
        listView!!.onItemLongClickListener = this
    }

    override fun onResume() {
        super.onResume()
        updateList()
    }

    private fun deleteLogFile(file: File): Boolean {
        Log.d(TAG, "Delete file path: " + file.name)
        return file.delete()
    }

    private val fileNameList: Array<String?>?
        private get() {
            val file = getLogDir(applicationContext).listFiles()
            if (file == null) {
                Log.w(TAG, "File not found")
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
            Log.w(TAG, "File not found")
            return
        }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            files
        )
        listView!!.adapter = adapter
    }

    override fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
        val listView = adapterView as ListView
        val selectedItem = listView.getItemAtPosition(position) as String
        val context = applicationContext
        val targetFile = File(getLogDir(context), selectedItem)
        val intent = Intent(applicationContext, LogViewActivity::class.java)
        intent.putExtra(Constants.EXTRA_LOG_FILE, targetFile)
        startActivity(intent)
    }

    override fun onItemLongClick(
        adapterView: AdapterView<*>,
        view: View,
        position: Int,
        id: Long
    ): Boolean {
        val listView = adapterView as ListView
        val selectedItem = listView.getItemAtPosition(position) as String
        AlertDialog.Builder(this@LogListViewActivity)
            .setTitle(resources.getString(R.string.delete_log_file_title))
            .setMessage(
                """
    ${resources.getString(R.string.delete_log_file_text)}
    ${resources.getString(R.string.file_name)}: $selectedItem
    """.trimIndent()
            )
            .setPositiveButton(
                android.R.string.ok
            ) { dialog, which ->
                val context = applicationContext
                val targetFile = File(getLogDir(context), selectedItem)
                if (deleteLogFile(targetFile)) {
                    updateList()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
        return true
    }

    companion object {
        private const val TAG = "LogListViewActivity"
    }
}