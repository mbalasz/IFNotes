package com.example.mateusz.ifnotes.chart

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class EatingLogsChartViewModelTest {
    @Mock private lateinit var repository: Repository
    private lateinit var eatingLogsChartViewModel: EatingLogsChartViewModel

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
    }

    @Test
    fun init() {
        val fastingWindowsHours = listOf(
            16L,
            13L,
            18L,
            10L
        )

        val eatingLogs = createEatingLogsWithFastingWindows(fastingWindowsHours)

        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        eatingLogsChartViewModel =
            EatingLogsChartViewModel(ApplicationProvider.getApplicationContext(), repository)

        runOnUiThread {
            eatingLogsChartViewModel.eatingLogsChartDataLiveData.observeForever {
                val entryPoints = it.entryPoints
                assertThat(entryPoints.size, `is`(equalTo(fastingWindowsHours.size)))
                for (i in entryPoints.indices) {
                    assertThat(
                        entryPoints[i].y,
                        `is`(equalTo(TimeUnit.HOURS.toMinutes(fastingWindowsHours[i]) / 60f)))
                }
            }
        }
    }

    private fun createEatingLogsWithFastingWindows(fastingWindowsHours: List<Long>): List<EatingLog> {
        var prevLog = EatingLog(startTime = 100L, endTime = 300L)
        val eatingLogs = mutableListOf(prevLog)
        fastingWindowsHours.forEach {
            val newLogStartTime = prevLog.endTime + TimeUnit.HOURS.toMillis(it)
            prevLog = EatingLog(startTime = newLogStartTime, endTime = newLogStartTime + 100L)
            eatingLogs.add(prevLog)
        }
        return eatingLogs
    }
}
