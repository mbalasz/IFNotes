package com.example.mateusz.ifnotes.data.room

import com.example.mateusz.ifnotes.domain.common.Mapper
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import javax.inject.Inject


class EatingLogDataMapper @Inject constructor() : Mapper<EatingLogData, EatingLog>() {
    override fun mapFrom(from: EatingLogData): EatingLog {
        val startTime = from.startTime?.let {
            LogDate(it.dateTimeInMillis, it.timezone)
        }
        val endTime = from.endTime?.let {
            LogDate(it.dateTimeInMillis, it.timezone)
        }

        return EatingLog(
            id = from.id,
            startTime = startTime,
            endTime = endTime)
    }

}
