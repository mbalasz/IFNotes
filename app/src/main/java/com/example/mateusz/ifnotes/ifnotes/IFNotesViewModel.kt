package com.example.mateusz.ifnotes.ifnotes

import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.mateusz.ifnotes.eatinglogs.ui.EatingLogsActivity
import com.example.mateusz.ifnotes.chart.ui.EatingLogsChartActivity
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.lib.EatingLogValidator
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.model.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class IFNotesViewModel @Inject constructor(application: Application, private val repository: Repository): AndroidViewModel(application) {
    companion object {
        private val DARK_GREEN = Color.parseColor("#a4c639")
        private val DARK_RED = Color.parseColor("#8b0000")
        private val BLACK = Color.BLACK

        const val SHORT_TIME_MS = 900_000L
        const val MID_TIME_MS = 1_800_000L
        const val LONG_TIME_MS = 3_600_000L
    }
    enum class LogState {
        FIRST_MEAL,
        LAST_MEAL,
        NO_CURRENT_LOG
    }

    data class TimeSinceLastActivityChronometerData(val baseTime: Long, val color: Int)

    data class LogTimeValidationMessage(val message: String)

    data class EatingLogDisplay(val logState: LogState, val logTime: String)

    private val eatingLogValidator = EatingLogValidator()
    private val currentEatingLogLiveData = MutableLiveData<EatingLog>()
    private val logTimeValidationMessageLiveData =
            MutableLiveData<LogTimeValidationMessage>()

    val startActivityData: LiveData<Event<Intent>>
        get() = _startActivityLiveData
    private val _startActivityLiveData = MutableLiveData<Event<Intent>>()

    val logButtonState = Transformations.map(currentEatingLogLiveData) { eatingLog ->
        eatingLog?.let {
            if (eatingLogValidator.isEatingLogFinished(eatingLog)) {
                LogState.FIRST_MEAL
            } else {
                LogState.LAST_MEAL
            }
        } ?: run { LogState.FIRST_MEAL }
    }
    val timeSinceLastActivity = Transformations.map(currentEatingLogLiveData) { eatingLog ->
        eatingLog?.let {
            if (eatingLogValidator.isEatingLogFinished(eatingLog)) {
                TimeSinceLastActivityChronometerData(
                        getElapsedRealTimeSinceBaseInMillis(eatingLog.endTime),
                        DARK_GREEN)
            } else {
                TimeSinceLastActivityChronometerData(
                        getElapsedRealTimeSinceBaseInMillis(eatingLog.startTime),
                        DARK_RED)
            }
        } ?: run { null }
    }
    val currentEatingLogDisplayLiveData =
            Transformations.map(currentEatingLogLiveData) { eatingLog ->
                eatingLog?.let {
                    val simpleDateFormat =
                            SimpleDateFormat("dd/M/yyyy HH:mm:ss", Locale.ENGLISH)
                    if (eatingLogValidator.isEatingLogFinished(eatingLog)) {
                        EatingLogDisplay(
                                LogState.LAST_MEAL, simpleDateFormat.format(eatingLog.endTime))
                    } else {
                        EatingLogDisplay(
                                LogState.FIRST_MEAL,
                                simpleDateFormat.format(eatingLog.startTime))
                    }
                } ?: run { EatingLogDisplay(LogState.NO_CURRENT_LOG, "") }
    }

    init {
        async(UI) {
            val mostRecentEatingLogDeferred = async { repository.getMostRecentEatingLog() }
            val mostRecentEatingLogFlowable = mostRecentEatingLogDeferred.await()
            mostRecentEatingLogFlowable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val eatingLog = it.orNull()
                        if (currentEatingLogLiveData.value != eatingLog) {
                            currentEatingLogLiveData.value = eatingLog
                        }
                    }
        }
    }

    fun getLogTimeValidationMessageLiveData(): LiveData<LogTimeValidationMessage> {
        return logTimeValidationMessageLiveData
    }

    fun onNewManualLog(hour: Int, minute: Int) {
        maybeUpdateCurrentEatingLog(DateTimeUtils.timeToMillis(hour, minute))
    }

    fun onLogButtonClicked() {
        updateCurrentEatingLog(getCurrentCalendarTime())
    }

    fun onLogShortTimeAgoClicked() {
        maybeUpdateCurrentEatingLog(getCurrentCalendarTime() - SHORT_TIME_MS)
    }

    fun onLogMidTimeAgoClicked() {
        maybeUpdateCurrentEatingLog(getCurrentCalendarTime() - MID_TIME_MS)
    }

    fun onLogLongTimeAgoClicked() {
        maybeUpdateCurrentEatingLog(getCurrentCalendarTime() - LONG_TIME_MS)
    }

    fun onHistoryButtonClicked() {
        val intent = Intent(getApplication(), EatingLogsActivity::class.java)
        _startActivityLiveData.value = Event(intent)
    }

    fun onChartButtonClicked() {
        val intent = Intent(getApplication(), EatingLogsChartActivity::class.java)
        _startActivityLiveData.value = Event(intent)
    }

    private fun maybeUpdateCurrentEatingLog(newLogTime: Long) {
        if (validateNewLogTime(newLogTime)) {
            updateCurrentEatingLog(newLogTime)
        }
    }

    private fun validateNewLogTime(logTime: Long): Boolean {
        when (eatingLogValidator.validateNewLogTime(
                logTime, currentEatingLogLiveData.value)) {
            EatingLogValidator.NewLogTimeValidationStatus.SUCCESS -> Unit
            EatingLogValidator.NewLogTimeValidationStatus.ERROR_TIME_TOO_EARLY -> {
                val validationMessage =
                        LogTimeValidationMessage(
                                message = "New log time cannot be sooner than the previous log" +
                                        " time")
                logTimeValidationMessageLiveData.value = validationMessage
                return false
            }
            EatingLogValidator.NewLogTimeValidationStatus.ERROR_TIME_IN_THE_FUTURE -> {
                val validationMessage =
                        LogTimeValidationMessage(message = "New log time cannot be in the future")
                logTimeValidationMessageLiveData.value = validationMessage
                return false
            }
        }
        return true
    }

    private fun updateCurrentEatingLog(logTime: Long) {
        val currentEatingLog = currentEatingLogLiveData.value
        val logTimeValidationStatus =
                eatingLogValidator.validateNewLogTime(logTime, currentEatingLog)
        if (logTimeValidationStatus != EatingLogValidator.NewLogTimeValidationStatus.SUCCESS) {
            throw IllegalStateException(
                    "Attempt to update most recent eating log with an invalid logTime:" +
                            " $logTimeValidationStatus")
        }
        val newEatingLog: EatingLog
        if (currentEatingLog == null || eatingLogValidator.isEatingLogFinished(currentEatingLog)) {
            newEatingLog = EatingLog(startTime = logTime)
            repository.insertEatingLog(newEatingLog)
        } else {
            newEatingLog = currentEatingLog.copy(endTime = logTime)
            repository.updateEatingLog(newEatingLog)
        }
        currentEatingLogLiveData.value = newEatingLog
    }

    /**
     * Calculates how much time has passed since {@param baseInMillis} and shifts that value in
     * reference to elapsed real time.
     */
    private fun getElapsedRealTimeSinceBaseInMillis(baseInMillis: Long): Long {
        return SystemClock.elapsedRealtime() - (System.currentTimeMillis() - baseInMillis)
    }

    private fun getCurrentCalendarTime(): Long {
        return System.currentTimeMillis()
    }
}