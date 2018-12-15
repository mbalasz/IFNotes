package com.example.mateusz.ifnotes

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mateusz.ifnotes.model.ifnotes.IFNotesViewModel
import com.example.mateusz.ifnotes.model.ifnotes.IFNotesViewModel.Companion.LONG_TIME_MS
import com.example.mateusz.ifnotes.model.ifnotes.IFNotesViewModel.Companion.MID_TIME_MS
import com.example.mateusz.ifnotes.model.ifnotes.IFNotesViewModel.Companion.SHORT_TIME_MS
import kotlinx.android.synthetic.main.activity_ifnotes.*
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

class IFNotesActivity : AppCompatActivity(), DateTimeDialogFragment.DateTimeDialogListener{
    companion object {
        const val LOG_FIRST_MEAL_BUTTON_TEXT = "Log my first meal"
        const val LOG_LAST_MEAL_BUTTON_TEXT = "Log my last meal"

        const val TIME_OF_FIRST_MEAL_TEXT = "You ate first meal at"
        const val TIME_OF_LAST_MEAL_TEXT = "You ate last meal at"

        const val TIME_SINCE_FIRST_MEAL_TEXT = "Time since first meal"
        const val TIME_SINCE_LAST_MEAL_TEXT = "Time since last meal"
    }

    val ifNotesViewModel: IFNotesViewModel by lazy {
        ViewModelProviders.of(this).get(IFNotesViewModel::class.java)
    }
    private lateinit var lastActivityChronometerWrapper: LastActivityChronometerWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ifnotes)

        ifNotesViewModel.currentEatingLogDisplayLiveData.observe(
                this, Observer { eatingLogDisplay ->
            if (eatingLogDisplay == null) {
                throw IllegalStateException("EatingLogDisplay cannot be null")
            }
            var lastActivityLogText: String? = null
            var timeSinceLastActivityLabelText: String? = null
            when (eatingLogDisplay.logState) {
                IFNotesViewModel.LogState.FIRST_MEAL -> {
                    lastActivityLogText = TIME_OF_FIRST_MEAL_TEXT
                    timeSinceLastActivityLabelText = TIME_SINCE_FIRST_MEAL_TEXT
                }
                IFNotesViewModel.LogState.LAST_MEAL-> {
                    lastActivityLogText = TIME_OF_LAST_MEAL_TEXT
                    timeSinceLastActivityLabelText = TIME_SINCE_LAST_MEAL_TEXT
                }
            }
            lastActivityLog.text = "$lastActivityLogText: ${eatingLogDisplay.logTime}"
            timeSinceLastActivityLabel.text = timeSinceLastActivityLabelText
        })

        lastActivityChronometerWrapper =
                LastActivityChronometerWrapper(timeSinceLastActivityChronometer)
        ifNotesViewModel.timeSinceLastActivity.observe(this, Observer { data ->
            if (data != null) {
                lastActivityChronometerWrapper.setBase(data.baseTime)
                lastActivityChronometerWrapper.setColor(data.color)
                lastActivityChronometerWrapper.start()
            } else {
                throw IllegalStateException("Time since last activity cannot be null")
            }
        })

        logActivityButton.text = LOG_FIRST_MEAL_BUTTON_TEXT
        ifNotesViewModel.logButtonState.observe(this, Observer { state ->
            when (state) {
                IFNotesViewModel.LogState.FIRST_MEAL ->
                    logActivityButton.text = LOG_FIRST_MEAL_BUTTON_TEXT
                IFNotesViewModel.LogState.LAST_MEAL ->
                    logActivityButton.text = LOG_LAST_MEAL_BUTTON_TEXT
                else -> throw IllegalStateException("Incorrect log button state")
            }
        })
        logActivityButton.setOnClickListener { ifNotesViewModel.onLogButtonClicked() }

        manualLogButton.setOnClickListener {
            val manualLogDialogFragment = DateTimeDialogFragment()
            manualLogDialogFragment.show(supportFragmentManager, "manualLog")
        }

        logShortTimeAgo.text = "${TimeUnit.MILLISECONDS.toMinutes(SHORT_TIME_MS)} min ago"
        logShortTimeAgo.setOnClickListener {
            ifNotesViewModel.onLogShortTimeAgoClicked()
        }
        logMidTimeAgo.text = "${TimeUnit.MILLISECONDS.toMinutes(MID_TIME_MS)} min ago"
        logMidTimeAgo.setOnClickListener {
            ifNotesViewModel.onLogMidTimeAgoClicked()
        }
        logLongTimeAgo.text = "${TimeUnit.MILLISECONDS.toMinutes(LONG_TIME_MS)} min ago"
        logLongTimeAgo.setOnClickListener {
            ifNotesViewModel.onLogLongTimeAgoClicked()
        }


        ifNotesViewModel.getLogTimeValidationMessageLiveData().observe(
                this, Observer { validationMessage ->
            val validationDialogBuilder = AlertDialog.Builder(this)
            validationDialogBuilder
                    .setMessage(validationMessage.message)
                    .setCancelable(false)
                    .setPositiveButton("OK") {_, _ -> }
            validationDialogBuilder.create().show()
        })

        history.setOnClickListener {
            startActivity(Intent(this, EatingLogsActivity::class.java))
        }
    }

    override fun onTimeSaved(hour: Int, minute: Int) {
        ifNotesViewModel.onNewManualLog(hour, minute)
    }

}
