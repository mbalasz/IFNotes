package com.example.mateusz.ifnotes.eatinglogs.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mateusz.ifnotes.R
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel

class EatingLogsAdapter(val context: Context, private val eatingLogsViewModel: EatingLogsViewModel)
    : RecyclerView.Adapter<EatingLogsAdapter.EatingLogViewHolder>() {

    inner class EatingLogViewHolder(val view: ViewGroup):
            RecyclerView.ViewHolder(view), EatingLogsViewModel.EatingLogsItemView {

        val startTimeTextView = view.findViewById(R.id.eating_log_start_time) as TextView
        val endTimeTextView = view.findViewById(R.id.eating_log_end_time) as TextView
        val removeButton = view.findViewById(R.id.item_row_remove_button) as ImageButton
        val editButton = view.findViewById(R.id.item_row_edit_button) as ImageButton

        init {
            removeButton.setOnClickListener {
                eatingLogsViewModel.onRemoveEatingLogItemClicked(this, adapterPosition)
            }
            editButton.setOnClickListener {
                eatingLogsViewModel.onEditEatingLogItemClicked(this, adapterPosition)
            }
        }

        override fun setStartTme(startTime: Long) {
            startTimeTextView.text = DateTimeUtils.toDateTimeString(startTime)
        }

        override fun setEndTime(endTime: Long) {
            endTimeTextView.text = DateTimeUtils.toDateTimeString(endTime)
        }

        override fun clearView() {
            startTimeTextView.text = ""
            endTimeTextView.text = ""
        }

        override fun notifyItemRemoved() {
            notifyItemRemoved(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EatingLogViewHolder {
        val viewGroup =
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.item_eating_log, parent, false) as ViewGroup
        return EatingLogViewHolder(viewGroup)
    }

    override fun getItemCount(): Int {
        return eatingLogsViewModel.getEatingLogsCount()
    }

    override fun onBindViewHolder(holder: EatingLogViewHolder, position: Int) {
        eatingLogsViewModel.onBindEatingLogsItemView(holder, position)
    }

    override fun onViewRecycled(holder: EatingLogViewHolder) {
        super.onViewRecycled(holder)
        eatingLogsViewModel.onEatingLogItemViewRecycled(holder)
    }
}