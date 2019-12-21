package com.example.mateusz.ifnotes.data

import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.google.common.base.Optional
import io.reactivex.Flowable

interface EatingLogsLocalDataSource {
    fun observeMostRecentEatingLog(): Flowable<Optional<EatingLog>>

    suspend fun getMostRecentEatingLog(): EatingLog?

    suspend fun insertEatingLog(eatingLog: EatingLog)

    suspend fun updateEatingLog(eatingLog: EatingLog)

    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
