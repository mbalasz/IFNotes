package com.example.mateusz.ifnotes.chart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.chart.EatingLogsChartDataProducer.DataPoint
import com.github.mikephil.charting.data.Entry
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class EatingLogsChartViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val MAX_WINDOW_HOURS = 19L
    }

    private val repository = Repository(application)

    data class ChartData(val entryPoints: List<Entry>, val labels: List<String>)

    val eatingLogsChartDataLiveData: LiveData<ChartData>
        get() = _eatingLogsChartDataLiveData
    private val _eatingLogsChartDataLiveData = MutableLiveData<ChartData>()
    private var eatingLogsSubscription: Disposable? = null
    private val windowValidator = WindowValidator(MAX_WINDOW_HOURS)


    init {
        eatingLogsSubscription = repository.getEatingLogsObservable().subscribe {
            val eatingLogs = it.sortedWith(
                    Comparator {a, b -> compareValuesBy(a, b, {it.startTime}, {it.endTime})})
            val dataPoints = FastingWindowChartDataProducer(windowValidator).getDataPoints(eatingLogs)
            var chartData =
                    ChartData(getEntriesFromDataPoints(dataPoints), getLabelsFromDataPoints(dataPoints))
            _eatingLogsChartDataLiveData.postValue(chartData)
        }
    }

    fun onDestroy() {
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
            labels.add(DateTimeUtils.toDateString(eatingLog.startTime))
        }
        return labels
    }
}