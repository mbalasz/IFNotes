package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.model.data.EatingLog
import java.lang.IllegalArgumentException

abstract class EatingLogsChartDataProducer(protected val timeWindowValidator: TimeWindowValidator) {

    abstract fun getDataPoints(eatingLogs: List<EatingLog>): List<DataPoint>

    data class DataPoint(val eatingLog: EatingLog, val windowDurationMs: Long)

    protected fun checkEatingLogValid(eatingLog: EatingLog) {
        if (eatingLog.startTime == null && eatingLog.endTime != null) {
            throw IllegalArgumentException(
                    "Cannot chart eating window for eatingLog: ${eatingLog.id} since it has" +
                            " end time but no start time.")
        }
    }
}
