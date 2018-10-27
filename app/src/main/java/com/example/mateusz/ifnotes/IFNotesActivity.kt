package com.example.mateusz.ifnotes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mateusz.ifnotes.model.EatingLogViewModel
import kotlinx.android.synthetic.main.activity_ifnotes.logActivityButton
import kotlinx.android.synthetic.main.activity_ifnotes.timeSinceLastActivityChronometer
import java.lang.IllegalStateException

class IFNotesActivity : AppCompatActivity() {
    companion object {
        const val LOG_FIRST_MEAL = "Log my first meal"
        const val LOG_LAST_MEAL = "Log my last meal"
    }

    val eatingLogViewModel: EatingLogViewModel by lazy {
        ViewModelProviders.of(this).get(EatingLogViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ifnotes)

        eatingLogViewModel.timeSinceLastActivity.observe(this, Observer { time ->
            if (time != null) {
                timeSinceLastActivityChronometer.base = time
                timeSinceLastActivityChronometer.start()
            } else {
                throw IllegalStateException("Time since last activity cannot be null")
            }
        })

        logActivityButton.text = LOG_FIRST_MEAL
        eatingLogViewModel.logButtonState.observe(this, Observer { state ->
            when (state) {
                EatingLogViewModel.LogButtonState.LOG_FIRST_MEAL ->
                    logActivityButton.text = LOG_FIRST_MEAL
                EatingLogViewModel.LogButtonState.LOG_LAST_MEAL ->
                    logActivityButton.text = LOG_LAST_MEAL
                else -> throw IllegalStateException("Incorrect log button state")
            }
        })
        logActivityButton.setOnClickListener { eatingLogViewModel.onLogButtonClicked() }
    }
}
