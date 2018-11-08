package com.example.mateusz.ifnotes.model.ifnotes

import android.app.Application
import android.graphics.Color
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.mateusz.ifnotes.model.EatingLog
import com.example.mateusz.ifnotes.model.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class IFNotesViewModel(application: Application): AndroidViewModel(application) {
    companion object {
        private val DARK_GREEN = Color.parseColor("#a4c639")
        private val DARK_RED = Color.parseColor("#8b0000")
    }
    enum class LogState {
        LOG_FIRST_MEAL,
        LOG_LAST_MEAL
    }

    data class TimeSinceLastActivityChronometerData(val baseTime: Long, val color: Int)

    data class LogTimeValidationMessage(val message: String)

    data class EatingLogDisplay(val logState: LogState, val logTime: String)

    private val repository = Repository(application)
    private val eatingLogHelper = EatingLogHelper()
    private val currentEatingLogLiveData = MutableLiveData<EatingLog>()
    private val logTimeValidationMessageLiveData =
            MutableLiveData<LogTimeValidationMessage>()
    val logButtonState = Transformations.map(currentEatingLogLiveData) { eatingLog ->
        eatingLog?.let {
            if (eatingLogHelper.isEatingLogFinished(eatingLog)) {
                LogState.LOG_FIRST_MEAL
            } else {
                LogState.LOG_LAST_MEAL
            }
        } ?: run { LogState.LOG_FIRST_MEAL }
    }
    val timeSinceLastActivity = Transformations.map(currentEatingLogLiveData) { eatingLog ->
        eatingLog?.let {
            if (eatingLogHelper.isEatingLogFinished(eatingLog)) {
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
                    if (eatingLogHelper.isEatingLogFinished(eatingLog)) {
                        EatingLogDisplay(
                                LogState.LOG_LAST_MEAL, simpleDateFormat.format(eatingLog.endTime))
                    } else {
                        EatingLogDisplay(
                                LogState.LOG_FIRST_MEAL,
                                simpleDateFormat.format(eatingLog.startTime))
                    }
                } ?: run { null }
    }

    init {
        async(UI) {
            val mostRecentEatingLogDeferred = async { repository.getMostRecentEatingLog() }
            val mostRecentEatingLogFlowable = mostRecentEatingLogDeferred.await()
            mostRecentEatingLogFlowable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (currentEatingLogLiveData.value != it) {
                            currentEatingLogLiveData.value = it
                        }
                    }
        }
    }

    fun getLogTimeValidationMessageLiveData(): LiveData<LogTimeValidationMessage> {
        return logTimeValidationMessageLiveData
    }

    fun onNewManualLog(hour: Int, minute: Int) {
        val logTime = Calendar.getInstance()
        logTime.set(
                logTime.get(Calendar.YEAR),
                logTime.get(Calendar.MONTH),
                logTime.get(Calendar.DAY_OF_MONTH),
                hour,
                minute)
        when (eatingLogHelper.validateNewLogTime(
                logTime.timeInMillis, currentEatingLogLiveData.value)) {
            EatingLogHelper.LogTimeValidationStatus.SUCCESS -> Unit
            EatingLogHelper.LogTimeValidationStatus.ERROR_TIME_TOO_EARLY -> {
                val validationMessage =
                        LogTimeValidationMessage(
                                message = "New log time cannot be sooner than the previous log" +
                                        " time")
                logTimeValidationMessageLiveData.value = validationMessage
                return
            }
            EatingLogHelper.LogTimeValidationStatus.ERROR_TIME_IN_THE_FUTURE -> {
                val validationMessage =
                        LogTimeValidationMessage(message = "New log time cannot be in the future")
                logTimeValidationMessageLiveData.value = validationMessage
                return
            }
        }
        updateCurrentEatingLog(logTime.timeInMillis)
    }

    fun onLogButtonClicked() {
        updateCurrentEatingLog(getCurrentCalendarTime())
    }

    private fun updateCurrentEatingLog(logTime: Long) {
        val currentEatingLog = currentEatingLogLiveData.value
        val logTimeValidationStatus =
                eatingLogHelper.validateNewLogTime(logTime, currentEatingLog)
        if (logTimeValidationStatus != EatingLogHelper.LogTimeValidationStatus.SUCCESS) {
            throw IllegalStateException(
                    "Attempt to update most recent eating log with an invalid logTime:" +
                            " $logTimeValidationStatus")
        }
        val newEatingLog: EatingLog
        if (currentEatingLog == null || eatingLogHelper.isEatingLogFinished(currentEatingLog)) {
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