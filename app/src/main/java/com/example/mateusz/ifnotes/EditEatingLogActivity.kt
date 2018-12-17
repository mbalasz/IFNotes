package com.example.mateusz.ifnotes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.model.editlog.EditEatingLogViewModel
import kotlinx.android.synthetic.main.activity_edit_eating_log.discardButton
import kotlinx.android.synthetic.main.activity_edit_eating_log.editFirstMealButton
import kotlinx.android.synthetic.main.activity_edit_eating_log.editLastMealButton
import kotlinx.android.synthetic.main.activity_edit_eating_log.saveButton
import kotlinx.android.synthetic.main.activity_edit_eating_log.timeOfFirstMealTextView
import kotlinx.android.synthetic.main.activity_edit_eating_log.timeOfLastMealTextView

class EditEatingLogActivity : AppCompatActivity(), DateTimeDialogFragment.DateTimeDialogListener {
    private val editEatingLogViewModel: EditEatingLogViewModel by lazy {
        ViewModelProviders.of(this).get(EditEatingLogViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_eating_log)

        editEatingLogViewModel.onActivityCreated(intent)

        editFirstMealButton.setOnClickListener {
            editEatingLogViewModel.onEditFirstMealButtonClicked()
        }
        editLastMealButton.setOnClickListener {
            editEatingLogViewModel.onEditLastMealButtonClicked()
        }

        editEatingLogViewModel.firstMealLogTimeObservable.observe(this, Observer {
            timeOfFirstMealTextView.text = DateTimeUtils.toDateTime(it)
        })
        editEatingLogViewModel.lastMealLogTimeObservable.observe(this, Observer {
            timeOfLastMealTextView.text = DateTimeUtils.toDateTime(it)
        })

        editEatingLogViewModel.showDateTimeDialogFragment.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                showDateTimeDialogFragment()
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

    fun showDateTimeDialogFragment() {
        val manualLogDialogFragment = DateTimeDialogFragment()
        manualLogDialogFragment.show(supportFragmentManager, "manualLog")
    }

    override fun onTimeSaved(hour: Int, minute: Int) {
        editEatingLogViewModel.onEatingLogEdited(hour, minute)
    }
}
