package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.model.data.EatingLog

class FastingWindowChartDataProducer(timeWindowValidator: TimeWindowValidator)
    : EatingLogsChartDataProducer(timeWindowValidator) {
    override fun getDataPoints(eatingLogs: List<EatingLog>): List<DataPoint> {
        if (eatingLogs.isEmpty() || eatingLogs.size == 1) {
            return emptyList()
        }

        checkEatingLogValid(eatingLogs[0])
        val dataPoints = arrayListOf<DataPoint>()
        for (i in eatingLogs.indices) {
            if (i == 0) {
                continue
            }
            val currEatingLog = eatingLogs[i]
            checkEatingLogValid(currEatingLog)
            val prevEatingLog = eatingLogs[i - 1]
            if (currEatingLog.startTime == null || prevEatingLog.endTime == null) {
                continue
            }
            val timeWindow = currEatingLog.startTime.dateTimeInMillis - prevEatingLog.endTime.dateTimeInMillis
            if (timeWindowValidator.isTimeWindowValid(timeWindow)) {
                dataPoints.add(DataPoint(currEatingLog, timeWindow))
            }
        }
        return dataPoints
    }
}
