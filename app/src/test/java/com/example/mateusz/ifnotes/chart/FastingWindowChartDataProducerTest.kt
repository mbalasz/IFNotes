package com.example.mateusz.ifnotes.chart

import com.example.mateusz.ifnotes.chart.EatingLogsChartDataProducer.DataPoint
import com.example.mateusz.ifnotes.model.data.EatingLog
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
                    EatingLog(startTime = 100L, endTime = 200L),
                    EatingLog(endTime = 1000L))
            )
        }
    }

    @Test
    fun getDataPoints() {
        val logs = listOf(
            EatingLog(startTime = 100L, endTime = 200L),
            EatingLog(startTime = 300L, endTime = 400L),
            EatingLog(startTime = 550L, endTime = 600L),
            EatingLog(startTime = 780L, endTime = 800L)
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
            EatingLog(startTime = 100L, endTime = 200L),
            EatingLog(startTime = 300L, endTime = 400L),
            EatingLog(startTime = 400L + maxWindowHours + 1, endTime = 600L),
            EatingLog(startTime = 780L, endTime = 800L)
        )

        val expectedDataPoints = listOf(
            DataPoint(logs[1], 100L),
            DataPoint(logs[3], 180L)
        )

        assertThat(fastingWindowChartDataProducer.getDataPoints(logs), equalTo(expectedDataPoints))
    }
}
