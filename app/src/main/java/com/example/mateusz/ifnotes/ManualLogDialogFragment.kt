package com.example.mateusz.ifnotes

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_manual_log.*
import kotlinx.android.synthetic.main.dialog_manual_log.view.*
import java.lang.ClassCastException
import java.lang.IllegalStateException

class ManualLogDialogFragment : DialogFragment() {
    companion object {
        const val BUTTON_SAVE_STRING = "Save"
        const val BUTTON_CANCEL_STRING = "Cancel"
    }

    private lateinit var manualLogDialogListener: ManualLogDialogListener
    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null


    interface ManualLogDialogListener {
        fun onTimeSaved(hour: Int, minute: Int)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            manualLogDialogListener = context as ManualLogDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                    "${context.toString()} must implement ${ManualLogDialogListener::class}")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val alertDialogBuilder = AlertDialog.Builder(it)
            val dialogView = it.layoutInflater.inflate(R.layout.dialog_manual_log, null)
            alertDialogBuilder
                    .setMessage("Manual log")
                    .setView(dialogView)
                    .setPositiveButton(BUTTON_SAVE_STRING) { dialog, id ->
                        val timePicker = dialogView.timePicker
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            manualLogDialogListener.onTimeSaved(timePicker.hour, timePicker.minute)
                        } else {
                            manualLogDialogListener.onTimeSaved(
                                    timePicker.currentHour, timePicker.currentMinute)
                        }
                    }
                    .setNegativeButton(BUTTON_CANCEL_STRING) { dialog, id ->

                    }
            alertDialogBuilder.create()

        } ?: throw IllegalStateException("Acitivity cannot be null")

    }
}