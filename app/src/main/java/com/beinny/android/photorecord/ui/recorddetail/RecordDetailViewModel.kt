package com.beinny.android.photorecord.ui.recorddetail

import androidx.lifecycle.*
import com.beinny.android.photorecord.model.Record
import com.beinny.android.photorecord.repository.recorddetail.RecordRepository
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class RecordDetailViewModel : ViewModel() {
    private val recordRepository = RecordRepository.get()
    private val recordIdLiveData = MutableLiveData<UUID>()

    var recordLiveData: LiveData<Record?> =
        Transformations.switchMap(recordIdLiveData) { recordId -> recordRepository.getRecord(recordId) }

    fun loadRecord(recordId:UUID) {
        recordIdLiveData.value = recordId
    }

    fun saveRecord(record: Record){
        viewModelScope.launch {
            recordRepository.updateRecord(record)
        }
    }

    fun deleteRecord(record: Record){
        viewModelScope.launch {
            recordRepository.deleteRecord(record)
        }
    }

    // 사진 파일이 가르킬 위치(File객체)를 RecordDetailFragment에 제공.
    fun getPhotoFile(record: Record): File {
        return recordRepository.getPhotoFile(record)
    }
    fun getThumbFile(record: Record): File {
        return recordRepository.getThumbFile(record)
    }
    fun getTempFile(record: Record): File {
        return recordRepository.getTempFile(record)
    }
}