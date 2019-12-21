package com.example.mateusz.ifnotes.data.room

import androidx.room.withTransaction
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.data.EatingLogsLocalDataSource
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.google.common.base.Optional
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomEatingLogsLocalDatabase @Inject constructor(
    private val iFNotesDatabase: IFNotesDatabase,
    private val dataToEntitiyMapper: EatingLogDataMapper,
    private val entityToDataMapper: EntityToDataMapper,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher) : EatingLogsLocalDataSource {

    override fun observeMostRecentEatingLog(): Flowable<Optional<EatingLog>> {
        return iFNotesDatabase.eatingLogDao().observeMostRecentEatingLog().map { list ->
            if (list.isNotEmpty()) {
                Optional.of(list[0])
            } else {
                Optional.absent()
            }
        }.map {
            dataToEntitiyMapper.mapOptional(it)
        }
    }

    override suspend fun insertEatingLog(eatingLog: EatingLog) = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().insert(entityToDataMapper.mapFrom(eatingLog))
    }

    override suspend fun updateEatingLog(eatingLog: EatingLog) = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().update(entityToDataMapper.mapFrom(eatingLog))
    }

    override suspend fun getMostRecentEatingLog(): EatingLog? = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().getMostRecentEatingLog()?.let {
            dataToEntitiyMapper.mapFrom(it)
        }
    }

    override suspend fun <T> runInTransaction(block: suspend () -> T): T = withContext(ioDispatcher) {
        iFNotesDatabase.withTransaction(block)
    }
}
