package com.example.mateusz.ifnotes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mateusz.ifnotes.model.ifnotes.IFNotesViewModel
import kotlinx.android.synthetic.main.activity_ifnotes.history
import kotlinx.android.synthetic.main.activity_ifnotes.logActivityButton
import kotlinx.android.synthetic.main.activity_ifnotes.timeSinceLastActivityChronometer
import java.lang.IllegalStateException

class IFNotesActivity : AppCompatActivity() {
    companion object {
        const val LOG_FIRST_MEAL = "Log my first meal"
        const val LOG_LAST_MEAL = "Log my last meal"
    }

    val ifNotesViewModel: IFNotesViewModel by lazy {
        ViewModelProviders.of(this).get(IFNotesViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ifnotes)

        ifNotesViewModel.timeSinceLastActivity.observe(this, Observer { time ->
            if (time != null) {
                timeSinceLastActivityChronometer.base = time
                timeSinceLastActivityChronometer.start()
            } else {
                throw IllegalStateException("Time since last activity cannot be null")
            }
        })

        logActivityButton.text = LOG_FIRST_MEAL
        ifNotesViewModel.logButtonState.observe(this, Observer { state ->
            when (state) {
                IFNotesViewModel.LogButtonState.LOG_FIRST_MEAL ->
                    logActivityButton.text = LOG_FIRST_MEAL
                IFNotesViewModel.LogButtonState.LOG_LAST_MEAL ->
                    logActivityButton.text = LOG_LAST_MEAL
                else -> throw IllegalStateException("Incorrect log button state")
            }
        })
        logActivityButton.setOnClickListener { ifNotesViewModel.onLogButtonClicked() }

        history.setOnClickListener {
            startActivity(Intent(this, EatingLogsActivity::class.java))
        }
    }
}
