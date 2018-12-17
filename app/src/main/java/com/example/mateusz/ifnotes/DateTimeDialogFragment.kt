package com.example.mateusz.ifnotes

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_date_time.view.*
import java.lang.ClassCastException
import java.lang.IllegalStateException

class DateTimeDialogFragment : DialogFragment() {
    companion object {
        const val BUTTON_SAVE_STRING = "Save"
        const val BUTTON_CANCEL_STRING = "Cancel"
    }

    private lateinit var dateTimeDialogListener: DateTimeDialogListener

    interface DateTimeDialogListener {
        fun onTimeSaved(hour: Int, minute: Int)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            dateTimeDialogListener = context as DateTimeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                    "${context.toString()} must implement ${DateTimeDialogListener::class}")
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
                    .setPositiveButton(BUTTON_SAVE_STRING) { dialog, id ->
                        val timePicker = dialogView.timePicker
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            dateTimeDialogListener.onTimeSaved(timePicker.hour, timePicker.minute)
                        } else {
                            dateTimeDialogListener.onTimeSaved(
                                    timePicker.currentHour, timePicker.currentMinute)
                        }
                    }
                    .setNegativeButton(BUTTON_CANCEL_STRING) { dialog, id ->

                    }
            alertDialogBuilder.create()

        } ?: throw IllegalStateException("Acitivity cannot be null")

    }
}