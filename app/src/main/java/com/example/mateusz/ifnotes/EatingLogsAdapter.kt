package com.example.mateusz.ifnotes

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mateusz.ifnotes.model.EatingLog
import java.text.SimpleDateFormat

class EatingLogsAdapter(val context: Context)
    : RecyclerView.Adapter<EatingLogsAdapter.EatingLogViewHolder>() {
    var eatingLogs: List<EatingLog> = ArrayList()

    class EatingLogViewHolder(val view: ViewGroup): RecyclerView.ViewHolder(view)

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
        val simpleDateFormat = SimpleDateFormat.getDateTimeInstance()
        (holder.view.findViewById(R.id.eating_log_date_log) as TextView).text =
                "${simpleDateFormat.format(eatingLog.startTime)} " +
                "${simpleDateFormat.format(eatingLog.endTime)}"
    }

    fun setData(eatingLogs: List<EatingLog>) {
        this.eatingLogs = eatingLogs
        notifyDataSetChanged()
    }
}