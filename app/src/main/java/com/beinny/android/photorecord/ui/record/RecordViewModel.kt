package com.beinny.android.photorecord.ui.record

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beinny.android.photorecord.PhotoRecordApplication
import com.beinny.android.photorecord.model.Record
import com.beinny.android.photorecord.repository.recorddetail.RecordRepository
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class RecordViewModel(private val recordRepository: RecordRepository): ViewModel() {
    // private val recordRepository = RecordRepository.get()
    val recordListLiveData = recordRepository.getRecords()

    /** [새로운 레코드 추가] */
    fun addRecord (record: Record) {
        viewModelScope.launch {
            recordRepository.addRecord(record)
        }
    }

    /** [레코드 삭제] */
    fun deleteRecord (record: Record) {
        viewModelScope.launch {
            recordRepository.deleteRecord(record)
        }
    }

    fun initCheck(id:UUID, state:Boolean) {
        viewModelScope.launch {
            recordRepository.initCheck()
            recordRepository.changeCheck(id, state)
        }
    }

    fun changeCheck (id:UUID, state:Boolean) {
        viewModelScope.launch {
            recordRepository.changeCheck(id, state)
        }
    }

    fun deleteCheckedRecord() {
        viewModelScope.launch {
            recordRepository.deleteCheckedRecord()
        }
    }

    /*
    // 사진 파일이 가르킬 위치(File객체)를 RecordDetailFragment에 제공.
    fun getPhotoFile(record: Record): File {
        return recordRepository.getPhotoFile(record)
    }

    fun getThumbFile(record: Record): File {
        return recordRepository.getThumbFile(record)
    }
    */
}