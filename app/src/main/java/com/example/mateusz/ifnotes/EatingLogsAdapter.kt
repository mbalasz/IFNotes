package com.example.mateusz.ifnotes

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mateusz.ifnotes.model.EatingLog
import java.text.SimpleDateFormat
import java.util.Locale

class EatingLogsAdapter(val context: Context)
    : RecyclerView.Adapter<EatingLogsAdapter.EatingLogViewHolder>() {
    var eatingLogs: List<EatingLog> = ArrayList()

    inner class EatingLogViewHolder(val view: ViewGroup): RecyclerView.ViewHolder(view) {
        val startTimeTextView = view.findViewById(R.id.eating_log_start_time) as TextView
        val endTimeTextView = view.findViewById(R.id.eating_log_end_time) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EatingLogViewHolder {
        val viewGroup =
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.item_eating_log, parent, false) as ViewGroup
        return EatingLogViewHolder(viewGroup)
    }

    override fun getItemCount(): Int {
        return eatingLogs.size
    }

    override fun onBindViewHolder(holder: EatingLogViewHolder, position: Int) {
        val eatingLog = eatingLogs[position]
        val simpleDateFormat = SimpleDateFormat("dd/M/yyyy HH:mm:ss", Locale.ENGLISH)
        holder.startTimeTextView.text = simpleDateFormat.format(eatingLog.startTime)
        if (eatingLog.endTime > 0) {
            holder.endTimeTextView.text =
                    simpleDateFormat.format(eatingLog.endTime)
        }
    }

    override fun onViewRecycled(holder: EatingLogViewHolder) {
        super.onViewRecycled(holder)
        holder.startTimeTextView.text = ""
        holder.endTimeTextView.text = ""
    }

    fun setData(eatingLogs: List<EatingLog>) {
        this.eatingLogs = eatingLogs
        notifyDataSetChanged()
    }
}