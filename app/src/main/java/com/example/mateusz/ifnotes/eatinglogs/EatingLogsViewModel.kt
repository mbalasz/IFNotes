package com.example.mateusz.ifnotes.eatinglogs

import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mateusz.ifnotes.eatinglogs.editlog.ui.EditEatingLogActivity
import com.example.mateusz.ifnotes.lib.BackupManager
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogViewModel
import kotlinx.coroutines.experimental.async
import javax.inject.Inject

class EatingLogsViewModel @Inject constructor (
    application: Application,
    private val repository: Repository
) : AndroidViewModel(application) {
    companion object {
        const val CHOOSE_CSV_LOGS_REQUEST_CODE = 1
        const val EDIT_EATING_LOG_REQUEST_CODE = 2
        const val CHOOSE_DIR_TO_EXPORT_CSV_CODE = 3

        private const val CSV_FILE_DEFAULT_NAME = "eating_logs"
    }

    data class ActivityForResultsData(val intent: Intent, val requestCode: Int)

    private val csvLogsManager = CSVLogsManager(application)
    private val backupManager = BackupManager(application)
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
                    Comparator { a, b -> compareValuesBy(b, a, { it.startTime }, { it.endTime }) })
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

    fun onExportLogs() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
            putExtra(
                    Intent.EXTRA_TITLE,
                    "${CSV_FILE_DEFAULT_NAME}_${DateTimeUtils.toDateString(System.currentTimeMillis())}.csv")
        }
        _startActivityForResult.value =
                Event(ActivityForResultsData(intent, CHOOSE_DIR_TO_EXPORT_CSV_CODE))
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSE_CSV_LOGS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                data?.let {
                    async {
                        val eatingLogs = csvLogsManager.getEatingLogsFromCsv(it.data)
                        if (!eatingLogs.isEmpty()) {
                            repository.deleteAll().await()
                            for (eatingLog in eatingLogs) {
                                repository.insertEatingLog(eatingLog)
                            }
                        }
                    }
                }
            }
        } else if (requestCode == CHOOSE_DIR_TO_EXPORT_CSV_CODE) {
            if (resultCode == RESULT_OK) {
                data?.let {
                    backupManager.backupLogsToFile(
                            it.data, csvLogsManager.getCsvFromEatingLogs(eatingLogs))
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
