package com.example.mateusz.ifnotes.data.room

import com.example.mateusz.ifnotes.data.EatingLogsLocalDataSource
import com.example.mateusz.ifnotes.domain.common.Mapper
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.google.common.base.Optional
import io.reactivex.Flowable
import javax.inject.Inject

class RoomEatingLogsLocalDatabase @Inject constructor(
    private val iFNotesDatabase: IFNotesDatabase,
    private val dataToEntitiyMapper: EatingLogDataMapper) : EatingLogsLocalDataSource {

    override fun getMostRecentEatingLog(): Flowable<Optional<EatingLog>> {
        return iFNotesDatabase.eatingLogDao().getMostRecentEatingLog().map { list ->
            if (list.isNotEmpty()) {
                Optional.of(list[0])
            } else {
                Optional.absent()
            }
        }.map {
            dataToEntitiyMapper.mapOptional(it)
        }
    }
}
