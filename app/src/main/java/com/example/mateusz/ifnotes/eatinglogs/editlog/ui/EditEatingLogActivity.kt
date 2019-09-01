package com.example.mateusz.ifnotes.eatinglogs.editlog.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mateusz.ifnotes.R
import com.example.mateusz.ifnotes.component.ViewModelFactory
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogViewModel
import com.example.mateusz.ifnotes.time.DateDialogFragment
import com.example.mateusz.ifnotes.time.TimeDialogFragment
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_edit_eating_log.discardButton
import kotlinx.android.synthetic.main.activity_edit_eating_log.editFirstMealButton
import kotlinx.android.synthetic.main.activity_edit_eating_log.editLastMealButton
import kotlinx.android.synthetic.main.activity_edit_eating_log.saveButton
import kotlinx.android.synthetic.main.activity_edit_eating_log.timeOfFirstMealTextView
import kotlinx.android.synthetic.main.activity_edit_eating_log.timeOfLastMealTextView
import javax.inject.Inject

class EditEatingLogActivity :
        AppCompatActivity(),
        TimeDialogFragment.TimeDialogListener,
        DateDialogFragment.DateDialogListener {

    @Inject lateinit var viewModelFactory: ViewModelFactory

    private val editEatingLogViewModel: EditEatingLogViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(EditEatingLogViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_eating_log)

        AndroidInjection.inject(this)

        editEatingLogViewModel.onActivityCreated(intent)

        editFirstMealButton.setOnClickListener {
            editEatingLogViewModel.onEditFirstMeal()
        }

        editLastMealButton.setOnClickListener {
            editEatingLogViewModel.onEditLastMeal()
        }

        editEatingLogViewModel.firstMealLogTimeObservable.observe(this, Observer {
            timeOfFirstMealTextView.text = it
        })
        editEatingLogViewModel.lastMealLogTimeObservable.observe(this, Observer {
            timeOfLastMealTextView.text = it
        })

        editEatingLogViewModel.showDialogFragment.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                it.show(supportFragmentManager, "manualLogDialog")
            }
        })

        editEatingLogViewModel.finishActivity.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                finish()
            }
        })

        saveButton.setOnClickListener {
            editEatingLogViewModel.onSaveButtonClicked()
        }

        discardButton.setOnClickListener {
            editEatingLogViewModel.onDiscardButtonClicked()
        }
    }

    override fun onTimeEditCancelled() {
        editEatingLogViewModel.onTimeEditCancelled()
    }

    override fun onDateEditCancelled() {
        editEatingLogViewModel.onDateEditCancelled()
    }

    override fun onTimeSaved(hour: Int, minute: Int) {
        editEatingLogViewModel.onTimeSaved(hour, minute)
    }

    override fun onDateSaved(day: Int, month: Int, year: Int) {
        editEatingLogViewModel.onDateSaved(day, month, year)
    }
}
