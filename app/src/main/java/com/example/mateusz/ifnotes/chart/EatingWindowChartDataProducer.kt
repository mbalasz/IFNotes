package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.model.data.EatingLog

class EatingWindowChartDataProducer(timeWindowValidator: TimeWindowValidator)
    : EatingLogsChartDataProducer(timeWindowValidator) {
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
            val timeWindow = eatingLog.endTime - eatingLog.startTime
            if (timeWindowValidator.isTimeWindowValid(timeWindow)) {
                dataPoints.add(DataPoint(eatingLog, timeWindow))
            }
        }
        return dataPoints
    }
}
