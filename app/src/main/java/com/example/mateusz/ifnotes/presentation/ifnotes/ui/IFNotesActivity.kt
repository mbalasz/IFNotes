package com.example.mateusz.ifnotes.presentation.ifnotes.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mateusz.ifnotes.R
import com.example.mateusz.ifnotes.component.ViewModelFactory
import com.example.mateusz.ifnotes.presentation.ifnotes.IFNotesViewModel
import com.example.mateusz.ifnotes.presentation.ifnotes.IFNotesViewModel.Companion.LONG_TIME_MS
import com.example.mateusz.ifnotes.presentation.ifnotes.IFNotesViewModel.Companion.MID_TIME_MS
import com.example.mateusz.ifnotes.presentation.ifnotes.IFNotesViewModel.Companion.SHORT_TIME_MS
import com.example.mateusz.ifnotes.time.TimeDialogFragment
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_ifnotes.chart
import kotlinx.android.synthetic.main.activity_ifnotes.history
import kotlinx.android.synthetic.main.activity_ifnotes.lastActivityLog
import kotlinx.android.synthetic.main.activity_ifnotes.logActivityButton
import kotlinx.android.synthetic.main.activity_ifnotes.logLongTimeAgo
import kotlinx.android.synthetic.main.activity_ifnotes.logMidTimeAgo
import kotlinx.android.synthetic.main.activity_ifnotes.logShortTimeAgo
import kotlinx.android.synthetic.main.activity_ifnotes.manualLogButton
import kotlinx.android.synthetic.main.activity_ifnotes.timeSinceLastActivityChronometer
import kotlinx.android.synthetic.main.activity_ifnotes.timeSinceLastActivityLabel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class IFNotesActivity : AppCompatActivity(), TimeDialogFragment.TimeDialogListener {
    companion object {
        const val LOG_FIRST_MEAL_BUTTON_TEXT = "Log my first meal"
        const val LOG_LAST_MEAL_BUTTON_TEXT = "Log my last meal"

        const val LAST_ACTIVITY_FIRST_MEAL_TEXT = "You ate first meal at"
        const val LAST_ACTIVITY_LAST_MEAL_TEXT = "You ate last meal at"

        const val TIME_SINCE_FIRST_MEAL_TEXT = "Time since first meal"
        const val TIME_SINCE_LAST_MEAL_TEXT = "Time since last meal"

        const val LAST_ACTIVITY_NO_CURRENT_LOG_TEXT =
                "No log to display. Log your activity with the buttons below."
    }

    @Inject lateinit var viewModelFactory: ViewModelFactory

    private val ifNotesViewModel: IFNotesViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(IFNotesViewModel::class.java)
    }
    private lateinit var lastActivityChronometerWrapper: LastActivityChronometerWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ifnotes)

        AndroidInjection.inject(this)

        lastActivityChronometerWrapper =
            LastActivityChronometerWrapper(timeSinceLastActivityChronometer)
        initUi()

        ifNotesViewModel.currentEatingLogDisplayLiveData.observe(
                this, Observer { eatingLogDisplay ->
            if (eatingLogDisplay == null) {
                throw IllegalStateException("EatingLogDisplay cannot be null")
            }
            var lastActivityLogText: String? = null
            var timeSinceLastActivityLabelText: String? = null
            when (eatingLogDisplay.logState) {
                IFNotesViewModel.LogState.FIRST_MEAL -> {
                    lastActivityLogText = "$LAST_ACTIVITY_FIRST_MEAL_TEXT: ${eatingLogDisplay.logTime}"
                    timeSinceLastActivityLabelText = TIME_SINCE_FIRST_MEAL_TEXT
                }
                IFNotesViewModel.LogState.LAST_MEAL -> {
                    lastActivityLogText = "$LAST_ACTIVITY_LAST_MEAL_TEXT: ${eatingLogDisplay.logTime}"
                    timeSinceLastActivityLabelText = TIME_SINCE_LAST_MEAL_TEXT
                }
                IFNotesViewModel.LogState.NO_CURRENT_LOG -> {
                    lastActivityLogText = LAST_ACTIVITY_NO_CURRENT_LOG_TEXT
                    timeSinceLastActivityLabelText = ""
                }
            }
            lastActivityLog.text = lastActivityLogText
            timeSinceLastActivityLabel.text = timeSinceLastActivityLabelText
        })

        ifNotesViewModel.timeSinceLastActivity.observe(this, Observer { data ->
            if (data != null) {
                lastActivityChronometerWrapper.setBase(data.baseTime)
                lastActivityChronometerWrapper.setColor(data.color)
                lastActivityChronometerWrapper.start()
            } else {
                resetChronometer()
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
            val manualLogDialogFragment = TimeDialogFragment()
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
                    .setPositiveButton("OK") { _, _ -> }
            validationDialogBuilder.create().show()
        })

        ifNotesViewModel.startActivityData.observe(this, Observer { intentEvent ->
            intentEvent.getContentIfNotHandled()?.let {
                startActivity(it)
            }
        })

        history.setOnClickListener {
            ifNotesViewModel.onHistoryButtonClicked()
        }

        chart.setOnClickListener {
            ifNotesViewModel.onChartButtonClicked()
        }
    }

    private fun initUi() {
        lastActivityLog.text = LAST_ACTIVITY_NO_CURRENT_LOG_TEXT
        timeSinceLastActivityLabel.text = ""
        resetChronometer()
    }

    private fun resetChronometer() {
        lastActivityChronometerWrapper.reset()
        lastActivityChronometerWrapper.setColor(Color.BLACK)
    }

    override fun onTimeEditCancelled() {
    }

    override fun onTimeSaved(hour: Int, minute: Int) {
        ifNotesViewModel.onNewManualLog(hour, minute)
    }
}
