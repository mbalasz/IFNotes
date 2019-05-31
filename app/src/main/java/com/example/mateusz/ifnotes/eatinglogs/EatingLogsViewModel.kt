package com.example.mateusz.ifnotes.eatinglogs

import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mateusz.ifnotes.component.AppModule.Companion.MainScope
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogViewModel
import com.example.mateusz.ifnotes.eatinglogs.editlog.ui.EditEatingLogActivity
import com.example.mateusz.ifnotes.lib.BackupManager
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.model.data.EatingLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

class EatingLogsViewModel @Inject constructor (
    application: Application,
    private val repository: Repository,
    @MainScope mainScope: CoroutineScope,
    private val csvLogsManager: CSVLogsManager,
    private val backupManager: BackupManager,
    private val clock: Clock
) : AndroidViewModel(application), CoroutineScope by mainScope {
    companion object {
        const val CHOOSE_CSV_LOGS_TO_IMPORT_REQUEST_CODE = 1
        const val EDIT_EATING_LOG_REQUEST_CODE = 2
        const val CHOOSE_CSV_FILE_TO_EXPORT_LOGS_CODE = 3

        private const val CSV_FILE_DEFAULT_NAME = "eating_logs"
    }

    data class ActivityForResultsData(val intent: Intent, val requestCode: Int)

    private var eatingLogs: List<EatingLog> = emptyList()
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

    fun onEditEatingLogItemClicked(position: Int) {
        if (position < 0 || eatingLogs.size - 1 < position) {
            return
        }
        val eatingLog = eatingLogs[position]
        val intent = Intent(getApplication(), EditEatingLogActivity::class.java).apply {
            putExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, eatingLog.id)
        }
        _startActivityForResult.value =
                Event(ActivityForResultsData(intent, EDIT_EATING_LOG_REQUEST_CODE))
    }

    fun onRemoveEatingLogItemClicked(position: Int) {
        if (position < 0 || eatingLogs.size - 1 < position) {
            return
        }
        eatingLogItemRemovedFlag = true
        launch {
            repository.deleteEatingLog(eatingLogs[position])
        }
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
                Event(ActivityForResultsData(intent, CHOOSE_CSV_LOGS_TO_IMPORT_REQUEST_CODE))
    }

    fun onExportLogs() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
            putExtra(
                    Intent.EXTRA_TITLE,
                    "${CSV_FILE_DEFAULT_NAME}_${DateTimeUtils.toDateString(clock.millis())}.csv")
        }
        _startActivityForResult.value =
                Event(ActivityForResultsData(intent, CHOOSE_CSV_FILE_TO_EXPORT_LOGS_CODE))
    }

    // TODO: add test for this method.
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSE_CSV_LOGS_TO_IMPORT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                data?.data?.let {
                    launch {
                        val eatingLogs = csvLogsManager.getEatingLogsFromCsv(it)
                        if (eatingLogs.isNotEmpty()) {
                            repository.deleteAll()
                            for (eatingLog in eatingLogs) {
                                repository.insertEatingLog(eatingLog)
                            }
                        }
                    }
                }
            }
        } else if (requestCode == CHOOSE_CSV_FILE_TO_EXPORT_LOGS_CODE) {
            if (resultCode == RESULT_OK) {
                data?.data?.let {
                    launch {
                        backupManager.backupLogsToFile(
                            it, csvLogsManager.createCsvFromEatingLogs(eatingLogs))
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
