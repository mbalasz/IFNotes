package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.model.data.EatingLog

class FastingWindowChartDataProducer(windowValidator: WindowValidator)
    : EatingLogsChartDataProducer(windowValidator) {
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
            val timeWindow = currEatingLog.startTime - prevEatingLog.endTime
            if (windowValidator.isTimeWindowValid(timeWindow)) {
                dataPoints.add(DataPoint(currEatingLog, timeWindow))
            }
        }
        return dataPoints
    }
}
