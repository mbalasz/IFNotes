package com.example.mateusz.ifnotes.time

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.lang.ClassCastException
import java.lang.IllegalStateException
import java.util.Calendar

class DateDialogFragment() : DialogFragment(), DatePickerDialog.OnDateSetListener {
    companion object {
        const val DATE_INIT_DAY = "DAY_INIT_DAY"
        const val DATE_INIT_MONTH = "DAY_INIT_MONTH"
        const val DATE_INIT_YEAR = "DAY_INIT_YEAR"
    }

    private lateinit var dateDialogListener: DateDialogListener

    interface DateDialogListener {
        fun onDateSaved(day: Int, month: Int, year: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            dateDialogListener = context as DateDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                    "$context must implement ${DateDialogListener::class}")
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        dateDialogListener.onDateSaved(dayOfMonth, month, year)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val c = Calendar.getInstance()
            var year = c.get(Calendar.YEAR)
            var month = c.get(Calendar.MONTH)
            var day = c.get(Calendar.DAY_OF_MONTH)
            arguments?.let {
                day = it.getInt(DATE_INIT_DAY)
                month = it.getInt(DATE_INIT_MONTH)
                year = it.getInt(DATE_INIT_YEAR)
            }
            DatePickerDialog(it, this, year, month, day)
        } ?: throw IllegalStateException("Acitivity cannot be null")
    }
}
