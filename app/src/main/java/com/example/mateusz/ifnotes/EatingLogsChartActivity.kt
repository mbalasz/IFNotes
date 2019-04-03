package com.example.mateusz.ifnotes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mateusz.ifnotes.model.chart.EatingLogsChartViewModel
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.content_chart.eatingLogsChart

class EatingLogsChartActivity : AppCompatActivity() {

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
            eatingLogsChart.data = LineData(dataSet)
            eatingLogsChart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return labels[value.toInt()]
                }
            }
            eatingLogsChart.invalidate()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        eatingLogsChartViewModel.onDestroy()
    }
}
