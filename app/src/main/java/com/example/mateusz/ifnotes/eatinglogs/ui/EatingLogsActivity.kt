package com.example.mateusz.ifnotes.eatinglogs.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mateusz.ifnotes.R
import com.example.mateusz.ifnotes.component.ViewModelFactory
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_eating_logs.toolbar
import kotlinx.android.synthetic.main.content_eating_logs.eatingLogsRecyclerView
import javax.inject.Inject

class EatingLogsActivity : AppCompatActivity() {

    @Inject lateinit var viewModelFactory: ViewModelFactory

    val eatingLogsViewModel: EatingLogsViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(EatingLogsViewModel::class.java)
    }
    lateinit var adapter: EatingLogsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eating_logs)

        AndroidInjection.inject(this)

        adapter = EatingLogsAdapter(this, eatingLogsViewModel)
        eatingLogsRecyclerView.adapter = adapter
        eatingLogsRecyclerView.layoutManager = LinearLayoutManager(this)

        eatingLogsViewModel.startActivityForResult.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                startActivityForResult(it.intent, it.requestCode)
            }
        })

        eatingLogsViewModel.refreshData.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                adapter.notifyDataSetChanged()
            }
        })

        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.eating_logs_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_import_logs -> {
                eatingLogsViewModel.onImportLogs()
                true
            } R.id.menu_item_export_logs -> {
                eatingLogsViewModel.onExportLogs()
                true
            } else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        eatingLogsViewModel.onActivityResult(requestCode, resultCode, data)
    }
}
