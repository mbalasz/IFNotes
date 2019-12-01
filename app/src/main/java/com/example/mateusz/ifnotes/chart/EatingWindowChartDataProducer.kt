package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.data.room.EatingLogData

class EatingWindowChartDataProducer(timeWindowValidator: TimeWindowValidator)
    : EatingLogsChartDataProducer(timeWindowValidator) {
    override fun getDataPoints(eatingLogData: List<EatingLogData>): List<DataPoint> {
        if (eatingLogData.isEmpty()) {
            return emptyList()
        }

        val dataPoints = arrayListOf<DataPoint>()
        for (eatingLog in eatingLogData) {
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
