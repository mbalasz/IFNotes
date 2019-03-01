package com.example.mateusz.ifnotes.model.eatinglogs

import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mateusz.ifnotes.EditEatingLogActivity
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.model.EatingLog
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.model.editlog.EditEatingLogViewModel
import kotlinx.coroutines.experimental.async

class EatingLogsViewModel(application: Application): AndroidViewModel(application) {
    companion object {
        const val CHOOSE_CSV_LOGS_REQUEST_CODE = 1
        const val EDIT_EATING_LOG_REQUEST_CODE = 2
    }

    data class ActivityForResultsData(val intent: Intent, val requestCode: Int)

    private val repository = Repository(application)
    private val csvLogsManager = CSVLogsManager(application)
    var eatingLogs: List<EatingLog> = emptyList()
    private val _startActivityForResult = MutableLiveData<Event<ActivityForResultsData>>()
    private val _refreshData = MutableLiveData<Event<Unit>>()
    private var eatingLogItemRemovedFlag = false

    val startActivityForResult: LiveData<Event<ActivityForResultsData>>
        get() = _startActivityForResult
    val refreshData: LiveData<Event<Unit>>
        get() = _refreshData

    init {
        repository.getEatingLogsObservable().subscribe {
            eatingLogs = it.sortedWith(
                    Comparator {a, b -> compareValuesBy(b, a, {it.startTime}, {it.endTime})})
            _refreshData.postValue(Event(Unit))
        }
    }

    fun onEatingLogItemViewRecycled(eatingLogsItemView: EatingLogsItemView) {
        eatingLogsItemView.clearView()
    }

    fun onEditEatingLogItemClicked(eatingLogsItemView: EatingLogsItemView, position: Int) {
        if (position < 0 || eatingLogs.size - 1 < position) {
            return
        }
        val eatingLog = eatingLogs[position]
        val intent = Intent(getApplication(), EditEatingLogActivity::class.java).apply {
            putExtra(EditEatingLogViewModel.EXTRA_LOG_TIME_ID, eatingLog.id)
        }
        _startActivityForResult.value =
                Event(ActivityForResultsData(intent, EDIT_EATING_LOG_REQUEST_CODE))
    }

    fun onRemoveEatingLogItemClicked(eatingLogsItemView: EatingLogsItemView, position: Int) {
        if (position < 0 || eatingLogs.size - 1 < position) {
            return
        }
        eatingLogItemRemovedFlag = true
        repository.deleteEatingLog(eatingLogs[position])
    }

    fun getEatingLogsCount(): Int {
        return eatingLogs.size
    }

    fun onBindEatingLogsItemView(eatingLogsItemView: EatingLogsItemView, position: Int) {
        if (position < 0 || eatingLogs.size - 1 < position) {
            return
        }
        val eatingLog = eatingLogs[position]
        eatingLogsItemView.setStartTme(eatingLog.startTime)
        if (eatingLog.endTime > 0) {
            eatingLogsItemView.setEndTime(eatingLog.endTime)
        }
    }

    fun onImportLogs() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        _startActivityForResult.value =
                Event(ActivityForResultsData(intent, CHOOSE_CSV_LOGS_REQUEST_CODE))
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSE_CSV_LOGS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                data?.let {
                    async {
                        val eatingLogs = csvLogsManager.getEatingLogsFromCsv(it.data)
                        if (!eatingLogs.isEmpty()) {
                            repository.deleteAll()
                            for (eatingLog in eatingLogs) {
                                repository.insertEatingLog(eatingLog)
                            }
                        }
                    }
                }
            }
        }
    }

    interface EatingLogsItemView {
        fun setStartTme(startTime: Long)

        fun setEndTime(endTime: Long)

        fun clearView()

        fun notifyItemRemoved()
    }
}