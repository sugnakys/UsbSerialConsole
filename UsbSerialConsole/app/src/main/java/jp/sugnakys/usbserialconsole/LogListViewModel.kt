package jp.sugnakys.usbserialconsole

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class LogListViewModel : ViewModel() {

    private val _fileList = MutableLiveData<List<File>>()
    val fileList = _fileList

    fun updateFileList(fileList: List<File>) {
        _fileList.postValue(fileList)
    }
}