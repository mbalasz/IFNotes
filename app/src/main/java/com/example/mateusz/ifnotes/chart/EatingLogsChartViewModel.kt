package com.example.mateusz.ifnotes.chart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mateusz.ifnotes.chart.EatingLogsChartDataProducer.DataPoint
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.model.Repository
import com.github.mikephil.charting.data.Entry
import io.reactivex.disposables.Disposable
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EatingLogsChartViewModel @Inject constructor(
    application: Application,
    repository: Repository
) : AndroidViewModel(application) {
    companion object {
        private const val MAX_WINDOW_HOURS = 19L
    }

    data class ChartData(val entryPoints: List<Entry>, val labels: List<String>)

    val eatingLogsChartDataLiveData: LiveData<ChartData>
        get() = _eatingLogsChartDataLiveData
    private val _eatingLogsChartDataLiveData = MutableLiveData<ChartData>()
    private var eatingLogsSubscription: Disposable? = null
    private val windowValidator = TimeWindowValidator(MAX_WINDOW_HOURS)

    init {
        eatingLogsSubscription = repository.getEatingLogsObservable().subscribe {
            val eatingLogs = it.sortedWith(
                    Comparator { a, b -> compareValuesBy(a, b, { it.startTime?.dateTimeInMillis }, { it.endTime?.dateTimeInMillis }) })
            val dataPoints = FastingWindowChartDataProducer(windowValidator).getDataPoints(eatingLogs)
            val chartData =
                    ChartData(getEntriesFromDataPoints(dataPoints), getLabelsFromDataPoints(dataPoints))
            _eatingLogsChartDataLiveData.postValue(chartData)
        }
    }

    override fun onCleared() {
        super.onCleared()
        eatingLogsSubscription?.dispose()
    }

    private fun getEntriesFromDataPoints(dataPoints: List<DataPoint>): List<Entry> {
        val entries = arrayListOf<Entry>()
        var xIdx = 0f
        for (dataPoint in dataPoints) {
            val windowDurationMs = dataPoint.windowDurationMs
            entries.add(Entry(xIdx++, TimeUnit.MILLISECONDS.toMinutes(windowDurationMs).toFloat() / 60f))
        }
        return entries
    }

    private fun getLabelsFromDataPoints(dataPoints: List<DataPoint>): List<String> {
        val labels = arrayListOf<String>()
        for (dataPoint in dataPoints) {
            val eatingLog = dataPoint.eatingLog
            eatingLog.startTime?.let {
                labels.add(DateTimeUtils.toDateString(it.dateTimeInMillis))
            } ?: throw IllegalStateException("DataPoint's EatingLog has no start time")
        }
        return labels
    }
}
