package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.domain.entity.EatingLog

class FastingWindowChartDataProducer(timeWindowValidator: TimeWindowValidator)
    : EatingLogsChartDataProducer(timeWindowValidator) {
    override fun getDataPoints(eatingLog: List<EatingLog>): List<DataPoint> {
        if (eatingLog.isEmpty() || eatingLog.size == 1) {
            return emptyList()
        }

        checkEatingLogValid(eatingLog[0])
        val dataPoints = arrayListOf<DataPoint>()
        for (i in eatingLog.indices) {
            if (i == 0) {
                continue
            }
            val currEatingLog = eatingLog[i]
            checkEatingLogValid(currEatingLog)
            val prevEatingLog = eatingLog[i - 1]
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
