package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.domain.entity.EatingLog

class EatingWindowChartDataProducer(timeWindowValidator: TimeWindowValidator)
    : EatingLogsChartDataProducer(timeWindowValidator) {
    override fun getDataPoints(eatingLogs: List<EatingLog>): List<DataPoint> {
        if (eatingLogs.isEmpty()) {
            return emptyList()
        }

        val dataPoints = arrayListOf<DataPoint>()
        for (eatingLog in eatingLogs) {
            checkEatingLogValid(eatingLog)
            if (eatingLog.endTime == null || eatingLog.startTime == null) {
                continue
            }
            val timeWindow = eatingLog.endTime.dateTimeInMillis - eatingLog.startTime.dateTimeInMillis
            if (timeWindowValidator.isTimeWindowValid(timeWindow)) {
                dataPoints.add(DataPoint(eatingLog, timeWindow))
            }
        }
        return dataPoints
    }
}
