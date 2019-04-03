package com.example.mateusz.ifnotes.model.chart

import com.example.mateusz.ifnotes.model.EatingLog
import java.lang.IllegalArgumentException

abstract class EatingLogsChartDataProducer {
    abstract fun getDataPoints(eatingLogs: List<EatingLog>): List<DataPoint>

    data class DataPoint(val eatingLog: EatingLog, val windowDurationMs: Long)

    protected fun checkEatingLogValid(eatingLog: EatingLog) {
        if (!eatingLog.hasStartTime() && eatingLog.hasEndTime()) {
            throw IllegalArgumentException(
                    "Cannot chart eating window for eatingLog: ${eatingLog.id} since it has" +
                            " end time but no start time.")
        }
    }
}