package com.example.mateusz.ifnotes.model.eatinglogs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.mateusz.ifnotes.model.EatingLog
import com.example.mateusz.ifnotes.model.Repository

class EatingLogsViewModel(application: Application): AndroidViewModel(application) {
    val repository = Repository(application)
    lateinit var eatingLogs: List<EatingLog>

    init {
        repository.getEatingLogsObservable().subscribe {
            eatingLogs = it.sortedWith(
                    Comparator {a, b -> compareValuesBy(b, a, {it.startTime}, {it.endTime})})
        }
    }

    fun onEatingLogItemViewRecycled(eatingLogsItemView: EatingLogsItemView) {
        eatingLogsItemView.clearView()
    }

    fun onRemoveEatingLogItemClicked(eatingLogsItemView: EatingLogsItemView, position: Int) {
        if (position < 0 || eatingLogs.size - 1 < position) {
            return
        }
        repository.deleteEatingLog(eatingLogs[position])
        eatingLogsItemView.notifyItemRemoved()
    }

    fun getEatingLogsCount(): Int {
        return eatingLogs.size
    }

    fun onBindEatingLogsItemView(eatingLogsItemView: EatingLogsItemView, position: Int) {
        val eatingLog = eatingLogs[position]
        eatingLogsItemView.setStartTme(eatingLog.startTime)
        if (eatingLog.endTime > 0) {
            eatingLogsItemView.setEndTime(eatingLog.endTime)
        }
    }

    interface EatingLogsItemView {
        fun setStartTme(startTime: Long)

        fun setEndTime(endTime: Long)

        fun clearView()

        fun notifyItemRemoved()
    }
}