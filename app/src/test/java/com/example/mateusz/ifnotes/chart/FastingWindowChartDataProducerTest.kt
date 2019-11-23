package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.chart.EatingLogsChartDataProducer.DataPoint
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.model.data.LogDate
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit
import kotlin.test.assertFailsWith

class FastingWindowChartDataProducerTest {
    companion object {
        private const val MAX_FAST_WINDOW_HR = 72L
    }

    private val fastingWindowChartDataProducer =
        FastingWindowChartDataProducer(TimeWindowValidator(MAX_FAST_WINDOW_HR))

    @Test
    fun getDataPoints_emptyLogs_returnsEmptyDataPoints() {
        assertThat(fastingWindowChartDataProducer.getDataPoints(emptyList()), equalTo(emptyList()))
    }

    @Test
    fun getDataPoints_singleLog_returnsEmptyDataPoints() {
        assertThat(
            fastingWindowChartDataProducer.getDataPoints(listOf(EatingLog())),
            equalTo(emptyList()))
    }

    @Test
    fun getDataPoints_invalidLogWithNoStartTime_throws() {
        assertFailsWith(IllegalArgumentException::class) {
            fastingWindowChartDataProducer.getDataPoints(
                listOf(
                    EatingLog(startTime = LogDate(100L), endTime = LogDate(200L)),
                    EatingLog(endTime = LogDate(1000L)))
            )
        }
    }

    @Test
    fun getDataPoints() {
        val logs = listOf(
            EatingLog(startTime = LogDate(100L), endTime = LogDate(200L)),
            EatingLog(startTime = LogDate(300L), endTime = LogDate(400L)),
            EatingLog(startTime = LogDate(550L), endTime = LogDate(600L)),
            EatingLog(startTime = LogDate(780L), endTime = LogDate(800L))
        )

        val expectedDataPoints = listOf(
            DataPoint(logs[1], 100L),
            DataPoint(logs[2], 150L),
            DataPoint(logs[3], 180L)
        )

        assertThat(fastingWindowChartDataProducer.getDataPoints(logs), equalTo(expectedDataPoints))
    }

    @Test
    fun getDataPoints_invalidTimeWindow_skipEatingLog() {
        val maxWindowHours = TimeUnit.HOURS.toMillis(MAX_FAST_WINDOW_HR)
        val logs = listOf(
            EatingLog(startTime = LogDate(100L), endTime = LogDate(200L)),
            EatingLog(startTime = LogDate(300), endTime = LogDate(400L)),
            EatingLog(startTime = LogDate(400L + maxWindowHours + 1), endTime = LogDate(600L)),
            EatingLog(startTime = LogDate(780L), endTime = LogDate(800L))
        )

        val expectedDataPoints = listOf(
            DataPoint(logs[1], 100L),
            DataPoint(logs[3], 180L)
        )

        assertThat(fastingWindowChartDataProducer.getDataPoints(logs), equalTo(expectedDataPoints))
    }
}
