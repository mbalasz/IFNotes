package com.example.mateusz.ifnotes.data

import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.data.room.EatingLogData
import com.example.mateusz.ifnotes.data.room.EatingLogDataMapper
import com.example.mateusz.ifnotes.data.room.IFNotesDatabase
import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.EatingLogValidator
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
        if (status == EatingLogValidator.NewLogValidationStatus.SUCCESS) {
            iFNotesDatabase.eatingLogDao().update(eatingLogData)
        }
        status
    }

    open suspend fun insertEatingLog(eatingLogData: EatingLogData) = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().insert(eatingLogData)
    }

    override suspend fun updateEatingLog(eatingLog: EatingLog) {
        localDataSource.updateEatingLog(eatingLog)
    }

    open suspend fun getEatingLog(id: Int): EatingLogData? = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().getEatingLog(id)
    }

    override fun observeMostRecentEatingLog(): Flowable<Optional<EatingLog>> =
        localDataSource.observeMostRecentEatingLog()


    override suspend fun getMostRecentEatingLog(): EatingLog? {
        return localDataSource.getMostRecentEatingLog()
    }

    override suspend fun insertEatingLog(eatingLog: EatingLog) {
        localDataSource.insertEatingLog(eatingLog)
    }

    open suspend fun deleteEatingLog(eatingLogData: EatingLogData) = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().delete(eatingLogData)
    }

    open suspend fun deleteAll() = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().deleteAll()
    }

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return localDataSource.runInTransaction(block)
    }

    private fun validateUpdate(eatingLogData: EatingLogData): EatingLogValidator.NewLogValidationStatus {
        val mutableLogs = iFNotesDatabase.eatingLogDao().getEatingLogs().toMutableList()
        val oldLog = mutableLogs.find { it.id == eatingLogData.id }
        oldLog?.let {
            mutableLogs.remove(it)
        }
        return eatingLogValidator.validateNewEatingLog(eatingLogDataMapper.mapFrom(eatingLogData),
            mutableLogs.map { eatingLogDataMapper.mapFrom(it) })
    }
}
