package com.example.mateusz.ifnotes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mateusz.ifnotes.model.ifnotes.IFNotesViewModel
import kotlinx.android.synthetic.main.activity_ifnotes.*
import java.lang.IllegalStateException

class IFNotesActivity : AppCompatActivity(), DateTimeDialogFragment.DateTimeDialogListener{
    companion object {
        const val LOG_FIRST_MEAL_BUTTON_TEXT = "Log my first meal"
        const val LOG_LAST_MEAL_BUTTON_TEXT = "Log my last meal"

        const val CURRENT_EATING_LOG_DISPLAY_FIRST_MEAL = "Time of first meal"
        const val CURRENT_EATING_LOG_DISPLAY_LAST_MEAL = "Time of last meal"
    }

    val ifNotesViewModel: IFNotesViewModel by lazy {
        ViewModelProviders.of(this).get(IFNotesViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ifnotes)

        ifNotesViewModel.currentEatingLogDisplayLiveData.observe(
                this, Observer { eatingLogDisplay ->
            if (eatingLogDisplay == null) {
                throw IllegalStateException("EatingLogDisplay cannot be null")
            }
            val logStateText = when (eatingLogDisplay.logState) {
                IFNotesViewModel.LogState.LOG_FIRST_MEAL ->
                    CURRENT_EATING_LOG_DISPLAY_FIRST_MEAL
                IFNotesViewModel.LogState.LOG_LAST_MEAL->
                    CURRENT_EATING_LOG_DISPLAY_LAST_MEAL
            }
            currentEatingLog.text = "$logStateText: ${eatingLogDisplay.logTime}"
        })

        ifNotesViewModel.timeSinceLastActivity.observe(this, Observer { time ->
            if (time != null) {
                timeSinceLastActivityChronometer.base = time
                timeSinceLastActivityChronometer.start()
            } else {
                throw IllegalStateException("Time since last activity cannot be null")
            }
        })

        logActivityButton.text = LOG_FIRST_MEAL_BUTTON_TEXT
        ifNotesViewModel.logButtonState.observe(this, Observer { state ->
            when (state) {
                IFNotesViewModel.LogState.LOG_FIRST_MEAL ->
                    logActivityButton.text = LOG_FIRST_MEAL_BUTTON_TEXT
                IFNotesViewModel.LogState.LOG_LAST_MEAL ->
                    logActivityButton.text = LOG_LAST_MEAL_BUTTON_TEXT
                else -> throw IllegalStateException("Incorrect log button state")
            }
        })
        logActivityButton.setOnClickListener { ifNotesViewModel.onLogButtonClicked() }

        manualLogButton.setOnClickListener {
            val manualLogDialogFragment = DateTimeDialogFragment()
            manualLogDialogFragment.show(supportFragmentManager, "manualLog")
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
