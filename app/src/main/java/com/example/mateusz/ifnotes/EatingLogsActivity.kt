package com.example.mateusz.ifnotes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mateusz.ifnotes.model.eatinglogs.EatingLogsViewModel
import kotlinx.android.synthetic.main.content_eating_logs.eatingLogsRecyclerView

class EatingLogsActivity : AppCompatActivity() {
    val eatingLogsViewModel: EatingLogsViewModel by lazy {
        ViewModelProviders.of(this).get(EatingLogsViewModel::class.java)
    }
    val adapter = EatingLogsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eating_logs)

        eatingLogsRecyclerView.adapter = adapter
        eatingLogsRecyclerView.layoutManager = LinearLayoutManager(this)
        eatingLogsViewModel.getEatingLogs().observe(this, Observer {
            adapter.setData(it)
        })
    }

}
