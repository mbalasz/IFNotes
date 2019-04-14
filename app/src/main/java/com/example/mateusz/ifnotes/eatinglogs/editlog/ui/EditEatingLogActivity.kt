package com.example.mateusz.ifnotes.eatinglogs.editlog.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mateusz.ifnotes.R
import com.example.mateusz.ifnotes.time.TimeDialogFragment
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogViewModel
import com.example.mateusz.ifnotes.time.DateDialogFragment
import kotlinx.android.synthetic.main.activity_edit_eating_log.*

class EditEatingLogActivity :
        AppCompatActivity(),
        TimeDialogFragment.TimeDialogListener,
        DateDialogFragment.DateDialogListener {

    private val editEatingLogViewModel: EditEatingLogViewModel by lazy {
        ViewModelProviders.of(this).get(EditEatingLogViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_eating_log)

        editEatingLogViewModel.onActivityCreated(intent)

        editFirstMealDateButton.setOnClickListener {
            editEatingLogViewModel.onEditFirstMealDateButtonClicked()
        }

        editFirstMealTimeButton.setOnClickListener {
            editEatingLogViewModel.onEditFirstMealTimeButtonClicked()
        }

        editLastMealDateButton.setOnClickListener {
            editEatingLogViewModel.onEditLastMealDateButtonClicked()
        }

        editLastMealTimeButton.setOnClickListener {
            editEatingLogViewModel.onEditLastMealTimeButtonClicked()
        }

        editEatingLogViewModel.firstMealLogTimeObservable.observe(this, Observer {
            timeOfFirstMealTextView.text = DateTimeUtils.toDateTimeString(it)
        })
        editEatingLogViewModel.lastMealLogTimeObservable.observe(this, Observer {
            timeOfLastMealTextView.text = DateTimeUtils.toDateTimeString(it)
        })

        editEatingLogViewModel.showTimeDialogFragment.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                showTimeDialogFragment()
            }
        })

        editEatingLogViewModel.showDateDialogFragment.observe(this, Observer {
            if (!it.hasBeenHandled) {
                showDateDialogFragment(it.getContentIfNotHandled())
            }
        })

        saveButton.setOnClickListener {
            editEatingLogViewModel.onSaveButtonClicked()
            finish()
        }

        discardButton.setOnClickListener {
            editEatingLogViewModel.onDiscardButtonClicked()
            finish()
        }
    }

    private fun showDateDialogFragment(initDate: Bundle?) {
        val manualLogDialogFragment = DateDialogFragment()
        manualLogDialogFragment.arguments = initDate
        manualLogDialogFragment.show(supportFragmentManager, "manualLog")
    }

    fun showTimeDialogFragment() {
        val manualLogDialogFragment = TimeDialogFragment()
        manualLogDialogFragment.show(supportFragmentManager, "manualLog")
    }

    override fun onTimeSaved(hour: Int, minute: Int) {
        editEatingLogViewModel.onEatingLogEdited(hour, minute)
    }

    override fun onDateSaved(day: Int, month: Int, year: Int) {
        editEatingLogViewModel.onEatingLogEdited(day, month, year)
    }
}
