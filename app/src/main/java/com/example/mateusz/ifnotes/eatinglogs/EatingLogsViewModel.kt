package com.example.mateusz.ifnotes.eatinglogs

import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogViewModel
import com.example.mateusz.ifnotes.eatinglogs.editlog.ui.EditEatingLogActivity
import com.example.mateusz.ifnotes.lib.BackupManager
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.data.EatingLogsRepositoryImpl
import com.example.mateusz.ifnotes.data.room.EatingLogData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

class EatingLogsViewModel @Inject constructor (
    application: Application,
    private val EatingLogsRepositoryImpl: EatingLogsRepositoryImpl,
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

    private var eatingLogData: List<EatingLogData> = emptyList()
    private val _startActivityForResult = MutableLiveData<Event<ActivityForResultsData>>()
    private val _refreshData = MutableLiveData<Event<Unit>>()
    private var eatingLogItemRemovedFlag = false

    val startActivityForResult: LiveData<Event<ActivityForResultsData>>
        get() = _startActivityForResult
    val refreshData: LiveData<Event<Unit>>
        get() = _refreshData

    init {
        EatingLogsRepositoryImpl.getEatingLogsObservable().subscribe {
            eatingLogData = it.sortedWith(
                    Comparator { a, b -> compareValuesBy(b, a, { it.startTime?.dateTimeInMillis }, { it.endTime?.dateTimeInMillis }) })
            _refreshData.postValue(Event(Unit))
        }
    }

    fun onEatingLogItemViewRecycled(eatingLogsItemView: EatingLogsItemView) {
        eatingLogsItemView.clearView()
    }

    fun onEditEatingLogItemClicked(position: Int) {
        if (position < 0 || eatingLogData.size - 1 < position) {
            return
        }
        val eatingLog = eatingLogData[position]
        val intent = Intent(getApplication(), EditEatingLogActivity::class.java).apply {
            putExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, eatingLog.id)
        }
        _startActivityForResult.value =
                Event(ActivityForResultsData(intent, EDIT_EATING_LOG_REQUEST_CODE))
    }

    fun onRemoveEatingLogItemClicked(position: Int) {
        if (position < 0 || eatingLogData.size - 1 < position) {
            return
        }
        eatingLogItemRemovedFlag = true
        launch {
            EatingLogsRepositoryImpl.deleteEatingLog(eatingLogData[position])
        }
    }

    fun getEatingLogsCount(): Int {
        return eatingLogData.size
    }

    fun onBindEatingLogsItemView(eatingLogsItemView: EatingLogsItemView, position: Int) {
        if (position < 0 || eatingLogData.size - 1 < position) {
            return
        }
        val eatingLog = eatingLogData[position]
        eatingLog.startTime?.let {startTime ->
            eatingLogsItemView.setStartTme(startTime.dateTimeInMillis)
            eatingLog.endTime?.let { endTime ->
                eatingLogsItemView.setEndTime(endTime.dateTimeInMillis)
            }
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

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSE_CSV_LOGS_TO_IMPORT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                data?.data?.let {
                    launch {
                        val eatingLogs = csvLogsManager.getEatingLogsFromCsv(it)
                        if (eatingLogs.isNotEmpty()) {
                            EatingLogsRepositoryImpl.deleteAll()
                            for (eatingLog in eatingLogs) {
                                EatingLogsRepositoryImpl.insertEatingLog(eatingLog)
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
                            it, csvLogsManager.createCsvFromEatingLogs(eatingLogData))
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
