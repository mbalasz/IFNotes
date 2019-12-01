package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.data.room.EatingLogData
import java.lang.IllegalArgumentException

abstract class EatingLogsChartDataProducer(protected val timeWindowValidator: TimeWindowValidator) {

    abstract fun getDataPoints(eatingLogData: List<EatingLogData>): List<DataPoint>

    data class DataPoint(val eatingLogData: EatingLogData, val windowDurationMs: Long)

    protected fun checkEatingLogValid(eatingLogData: EatingLogData) {
        if (eatingLogData.startTime == null && eatingLogData.endTime != null) {
            throw IllegalArgumentException(
                    "Cannot chart eating window for eatingLogData: ${eatingLogData.id} since it has" +
                            " end time but no start time.")
        }
    }
}
