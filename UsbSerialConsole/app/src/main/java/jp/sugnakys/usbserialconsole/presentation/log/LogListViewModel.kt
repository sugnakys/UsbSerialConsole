package jp.sugnakys.usbserialconsole.presentation.log

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import jp.sugnakys.usbserialconsole.log.LogRepository

@HiltViewModel
class LogListViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    private val _fileList = MutableLiveData<List<File>>()
    val fileList = _fileList

    fun updateFileList() {
        val fileList = logRepository.getLogDir()
            .listFiles()?.toList() ?: return
        _fileList.postValue(fileList)
    }
}