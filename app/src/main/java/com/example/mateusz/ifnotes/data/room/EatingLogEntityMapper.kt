package com.example.mateusz.ifnotes.data.room

import com.example.mateusz.ifnotes.domain.common.Mapper
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import javax.inject.Inject


class EatingLogEntityMapper @Inject constructor() : Mapper<EatingLog, EatingLogData>() {
    override fun mapFrom(from: EatingLog): EatingLogData {
        val startTime = from.startTime?.let {
            LogDateData(it.dateTimeInMillis, it.timezone)
        }
        val endTime = from.endTime?.let {
            LogDateData(it.dateTimeInMillis, it.timezone)
        }

        return EatingLogData(
            id = from.id,
            startTime = startTime,
            endTime = endTime)
    }

}
