package com.example.mateusz.ifnotes.chart.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mateusz.ifnotes.R
import com.example.mateusz.ifnotes.chart.EatingLogsChartViewModel
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.content_chart.eatingLogsChart

class EatingLogsChartActivity : AppCompatActivity() {

    companion object {
        private const val X_RANGE_MAX = 10f
        private const val GRANULARITY = 5f
        private const val VALUE_TEXT_SIZE = 10f
    }

    private val eatingLogsChartViewModel: EatingLogsChartViewModel by lazy {
        ViewModelProviders.of(this).get(EatingLogsChartViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eating_logs_chart)

        eatingLogsChartViewModel.eatingLogsChartDataLiveData.observe(this, Observer {
            val entryPoints = it.entryPoints
            val labels = it.labels
            val dataSet = LineDataSet(entryPoints, "EatingLogs")
            dataSet.valueTextSize = VALUE_TEXT_SIZE
            dataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "%.1f".format(value)
                }
            }
            eatingLogsChart.data = LineData(dataSet)
            eatingLogsChart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return labels[value.toInt()]
                }
            }
            eatingLogsChart.xAxis.granularity = GRANULARITY

            eatingLogsChart.axisLeft.axisMinimum = dataSet.yMin - 1f
            eatingLogsChart.axisLeft.axisMaximum = dataSet.yMax + 1f
            eatingLogsChart.axisRight.axisMinimum = dataSet.yMin - 1f
            eatingLogsChart.axisRight.axisMaximum = dataSet.yMax + 1f
            eatingLogsChart.setVisibleXRangeMaximum(X_RANGE_MAX)
            eatingLogsChart.moveViewToX(dataSet.xMax)
            eatingLogsChart.setVisibleXRangeMaximum(eatingLogsChart.xAxis.mAxisRange)
            eatingLogsChart.invalidate()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        eatingLogsChartViewModel.onDestroy()
    }
}
