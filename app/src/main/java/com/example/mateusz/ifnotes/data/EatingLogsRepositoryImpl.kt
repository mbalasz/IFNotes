package com.example.mateusz.ifnotes.data

import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.data.room.EatingLogData
import com.example.mateusz.ifnotes.data.room.EatingLogDataMapper
import com.example.mateusz.ifnotes.data.room.IFNotesDatabase
import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import com.example.mateusz.ifnotes.lib.EatingLogValidator
import com.google.common.base.Optional
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EatingLogsRepositoryImpl @Inject constructor(
    private val localDataSource: EatingLogsLocalDataSource,
    private val iFNotesDatabase: IFNotesDatabase,
    private val eatingLogValidator: EatingLogValidator,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val eatingLogDataMapper: EatingLogDataMapper
) : EatingLogsRepository {

    open fun getEatingLogsObservable(): Flowable<List<EatingLogData>> {
        return iFNotesDatabase.eatingLogDao().getEatingLogsFlowable()
    }

    open suspend fun updateEatingLog(eatingLogData: EatingLogData) = withContext(ioDispatcher) {
        val status = validateUpdate(eatingLogData)
        if (status == EatingLogValidator.EatingLogValidationStatus.SUCCESS) {
            iFNotesDatabase.eatingLogDao().update(eatingLogData)
        }
        status
    }

    open suspend fun insertEatingLog(eatingLogData: EatingLogData) = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().insert(eatingLogData)
    }

    open suspend fun getEatingLog(id: Int): EatingLogData? = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().getEatingLog(id)
    }

    override fun getMostRecentEatingLog(): Flowable<Optional<EatingLog>> =
        localDataSource.getMostRecentEatingLog()

    open suspend fun deleteEatingLog(eatingLogData: EatingLogData) = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().delete(eatingLogData)
    }

    open suspend fun deleteAll() = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().deleteAll()
    }

    private fun validateUpdate(eatingLogData: EatingLogData): EatingLogValidator.EatingLogValidationStatus {
        val mutableLogs = iFNotesDatabase.eatingLogDao().getEatingLogs().toMutableList()
        val oldLog = mutableLogs.find { it.id == eatingLogData.id }
        oldLog?.let {
            mutableLogs.remove(it)
        }
        return eatingLogValidator.validateNewEatingLog(eatingLogDataMapper.mapFrom(eatingLogData),
            mutableLogs.map { eatingLogDataMapper.mapFrom(it) })
    }
}
