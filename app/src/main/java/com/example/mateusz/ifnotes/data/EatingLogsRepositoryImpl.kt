package com.example.mateusz.ifnotes.data

import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.google.common.base.Optional
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EatingLogsRepositoryImpl @Inject constructor(
    private val localDataSource: EatingLogsLocalDataSource) : EatingLogsRepository {

    override fun observeEatingLogs(): Flowable<List<EatingLog>> =
        localDataSource.observeEatingLogs()

    override suspend fun getEatingLogs(): List<EatingLog> =
        localDataSource.getEatingLogs()

    override suspend fun updateEatingLog(eatingLog: EatingLog) =
        localDataSource.updateEatingLog(eatingLog)

    override suspend fun deleteEatingLog(eatingLog: EatingLog) =
        localDataSource.deleteEatingLog(eatingLog)

    override suspend fun deleteAllEatingLogs() =
        localDataSource.deleteAllEatingLogs()

    override suspend fun getEatingLog(eatingLogId: Int): EatingLog? =
        localDataSource.getEatingLog(eatingLogId)

    override fun observeMostRecentEatingLog(): Flowable<Optional<EatingLog>> =
        localDataSource.observeMostRecentEatingLog()


    override suspend fun getMostRecentEatingLog(): EatingLog? =
        localDataSource.getMostRecentEatingLog()

    override suspend fun insertEatingLog(eatingLog: EatingLog) =
        localDataSource.insertEatingLog(eatingLog)

    override suspend fun <T> runInTransaction(block: suspend () -> T): T =
        localDataSource.runInTransaction(block)
}
