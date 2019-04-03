package com.example.mateusz.ifnotes.model.chart

import android.util.Log
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.model.EatingLog

class EatingWindowChartDataProducer : EatingLogsChartDataProducer() {
    override fun getDataPoints(eatingLogs: List<EatingLog>): List<DataPoint> {
        if (eatingLogs.isEmpty()) {
            return emptyList()
        }

        val dataPoints = arrayListOf<DataPoint>()
        for (eatingLog in eatingLogs) {
            checkEatingLogValid(eatingLog)
            if (!eatingLog.hasEndTime()) {
                continue
            }
            dataPoints.add(DataPoint(eatingLog, eatingLog.endTime - eatingLog.startTime))
        }
        return dataPoints
    }
}