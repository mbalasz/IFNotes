package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.domain.entity.EatingLog

abstract class EatingLogsChartDataProducer(protected val timeWindowValidator: TimeWindowValidator) {

    abstract fun getDataPoints(eatingLog: List<EatingLog>): List<DataPoint>

    data class DataPoint(val eatingLog: EatingLog, val windowDurationMs: Long)

    protected fun checkEatingLogValid(eatingLog: EatingLog) {
        if (eatingLog.startTime == null && eatingLog.endTime != null) {
            throw IllegalArgumentException(
                    "Cannot chart eating window for eatingLog: ${eatingLog.id} since it has" +
                            " end time but no start time.")
        }
    }
}
