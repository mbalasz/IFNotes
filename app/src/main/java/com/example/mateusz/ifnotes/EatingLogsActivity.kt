package com.example.mateusz.ifnotes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mateusz.ifnotes.model.eatinglogs.EatingLogsViewModel
import kotlinx.android.synthetic.main.activity_eating_logs.toolbar
import kotlinx.android.synthetic.main.content_eating_logs.eatingLogsRecyclerView

class EatingLogsActivity : AppCompatActivity() {
    val eatingLogsViewModel: EatingLogsViewModel by lazy {
        ViewModelProviders.of(this).get(EatingLogsViewModel::class.java)
    }
    lateinit var adapter: EatingLogsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eating_logs)

        adapter = EatingLogsAdapter(this, eatingLogsViewModel)
        eatingLogsRecyclerView.adapter = adapter
        eatingLogsRecyclerView.layoutManager = LinearLayoutManager(this)

        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.eating_logs_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_import_logs -> {
                Toast.makeText(this, "Item clicked", Toast.LENGTH_LONG).show()
                true
            } else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}
