package com.example.mateusz.ifnotes.domain

import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.google.common.base.Optional
import io.reactivex.Flowable

interface EatingLogsRepository {
    // TODO: replace Flowable with Observable. There shouldn't be a need for backpressure strategy.
    // mostRecentEatingLog changes infrequently.
    fun observeMostRecentEatingLog(): Flowable<Optional<EatingLog>>

    fun observeEatingLogs(): Flowable<List<EatingLog>>

    suspend fun getEatingLog(eatingLogId: Int): EatingLog?

    suspend fun getMostRecentEatingLog(): EatingLog?

    suspend fun insertEatingLog(eatingLog: EatingLog)

    suspend fun updateEatingLog(eatingLog: EatingLog)

    suspend fun getEatingLogs(): List<EatingLog>

    suspend fun deleteEatingLog(eatingLog: EatingLog)

    suspend fun deleteAllEatingLogs()

    suspend fun <T> runInTransaction (block: suspend () -> T): T
}
