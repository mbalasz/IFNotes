package com.example.mateusz.ifnotes.time

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.mateusz.ifnotes.R
import kotlinx.android.synthetic.main.dialog_date_time.view.*
import java.lang.ClassCastException
import java.lang.IllegalStateException

class TimeDialogFragment : DialogFragment() {
    companion object {
        const val BUTTON_SAVE_STRING = "Save"
        const val BUTTON_CANCEL_STRING = "Cancel"
    }

    private lateinit var timeDialogListener: TimeDialogListener

    interface TimeDialogListener {
        fun onTimeSaved(hour: Int, minute: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            timeDialogListener = context as TimeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                    "$context must implement ${TimeDialogListener::class}")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val alertDialogBuilder = AlertDialog.Builder(it)
            val dialogView = it.layoutInflater.inflate(R.layout.dialog_date_time, null)
            dialogView.timePicker.setIs24HourView(true)
            alertDialogBuilder
                    .setMessage("Manual log")
                    .setView(dialogView)
                    .setPositiveButton(BUTTON_SAVE_STRING) { _, _ ->
                        val timePicker = dialogView.timePicker
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            timeDialogListener.onTimeSaved(timePicker.hour, timePicker.minute)
                        } else {
                            timeDialogListener.onTimeSaved(
                                    timePicker.currentHour, timePicker.currentMinute)
                        }
                    }
                    .setNegativeButton(BUTTON_CANCEL_STRING) { _, _ ->
                    }
            alertDialogBuilder.create()
        } ?: throw IllegalStateException("Acitivity cannot be null")
    }
}
