package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.data.room.EatingLogData

class FastingWindowChartDataProducer(timeWindowValidator: TimeWindowValidator)
    : EatingLogsChartDataProducer(timeWindowValidator) {
    override fun getDataPoints(eatingLogData: List<EatingLogData>): List<DataPoint> {
        if (eatingLogData.isEmpty() || eatingLogData.size == 1) {
            return emptyList()
        }

        checkEatingLogValid(eatingLogData[0])
        val dataPoints = arrayListOf<DataPoint>()
        for (i in eatingLogData.indices) {
            if (i == 0) {
                continue
            }
            val currEatingLog = eatingLogData[i]
            checkEatingLogValid(currEatingLog)
            val prevEatingLog = eatingLogData[i - 1]
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
