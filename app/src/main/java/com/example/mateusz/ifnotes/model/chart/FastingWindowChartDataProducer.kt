package com.example.mateusz.ifnotes.model.chart

import com.example.mateusz.ifnotes.model.EatingLog

class FastingWindowChartDataProducer : EatingLogsChartDataProducer() {
    override fun getDataPoints(eatingLogs: List<EatingLog>): List<DataPoint> {
        if (eatingLogs.isEmpty() || eatingLogs.size == 1) {
            return emptyList()
        }

        checkEatingLogValid(eatingLogs[0])
        val dataPoints = arrayListOf<DataPoint>()
        for (i in eatingLogs.indices) {
            if (i == 0 || i == eatingLogs.size - 1) {
                continue
            }
            val currEatingLog = eatingLogs[i]
            val prevEatingLog = eatingLogs[i - 1]
            checkEatingLogValid(currEatingLog)
            dataPoints.add(DataPoint(currEatingLog, currEatingLog.startTime - prevEatingLog.endTime))
        }
        return dataPoints
    }
}