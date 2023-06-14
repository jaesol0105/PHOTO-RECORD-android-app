package com.beinny.android.photorecord.repository.recorddetail

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.beinny.android.photorecord.model.Record
import com.beinny.android.photorecord.datebase.RecordDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "photorecord-database"

class RecordRepository private constructor(context: Context) {
    private val database: RecordDatabase = Room.databaseBuilder(
        context.applicationContext,
        RecordDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val recordDao = database.recordDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun getRecords(): LiveData<List<Record>> = recordDao.getRecords()
    fun getRecord(id: UUID): LiveData<Record?> = recordDao.getRecord(id)

    suspend fun updateRecord(record: Record) {
        // executor.execute
        withContext(ioDispatcher) {
            recordDao.updateRecord(record)
        }
    }

    suspend fun addRecord(record: Record) {
        withContext(ioDispatcher) {
            recordDao.addRecord(record)
        }
    }

    suspend fun deleteRecord(record: Record) {
        withContext(ioDispatcher) {
            recordDao.deleteRecord(record)
        }
    }

    suspend fun deleteAllRecord() {
        withContext(ioDispatcher) {
            recordDao.deleteAllRecord()
        }
    }

    suspend fun initCheck() {
        withContext(ioDispatcher) {
            recordDao.initCheck()
        }
    }

    suspend fun changeCheck(id: UUID, state: Boolean) {
        withContext(ioDispatcher) {
            recordDao.changeCheck(id, state)
        }
    }

    suspend fun deleteCheckedRecord() {
        withContext(ioDispatcher) {
            recordDao.deleteCheckedRecord()
        }
    }

    suspend fun deleteSelectedRecord(recordList: List<Record>) {
        withContext(ioDispatcher) {
            recordDao.deleteSelectedRecord(recordList)
        }
    }

    // photoFileName를 인자로 받고, 파일의 위치를 가리킬 File 객체를 반환
    fun getPhotoFile(record: Record): File = File(filesDir, record.photoFileName)

    // thumbFileName를 인자로 받고, 파일의 위치를 가리킬 File 객체를 반환
    fun getThumbFile(record: Record): File = File(filesDir, record.thumbFileName)

    // 이미지 업로드 임시저장용
    fun getTempFile(record: Record): File = File(filesDir, record.tempFileName)

    companion object {
        private var INSTANCE: RecordRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = RecordRepository(context)
            }
        }

        fun get(): RecordRepository {
            return INSTANCE ?: throw IllegalStateException("RecordRepository must be initialized!")
        }
    }
}