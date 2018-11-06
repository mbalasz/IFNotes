package com.example.mateusz.ifnotes.model.ifnotes

import android.app.Application
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
import java.util.Calendar

class IFNotesViewModel(application: Application): AndroidViewModel(application) {
    enum class LogButtonState {
        LOG_FIRST_MEAL,
        LOG_LAST_MEAL
    }

    data class LogTimeValidationMessage(val message: String)

    private val repository = Repository(application)
    private val eatingLogHelper = EatingLogHelper()
    private val currentEatingLogLiveData = MutableLiveData<EatingLog>()
    private val logTimeValidationMessageLiveData =
            MutableLiveData<LogTimeValidationMessage>()
    val logButtonState = Transformations.map(currentEatingLogLiveData) { eatingLog ->
        eatingLog?.let {
            if (eatingLogHelper.isEatingLogFinished(eatingLog)) {
                LogButtonState.LOG_FIRST_MEAL
            } else {
                LogButtonState.LOG_LAST_MEAL
            }
        } ?: run { LogButtonState.LOG_FIRST_MEAL }
    }
    val timeSinceLastActivity = Transformations.map(currentEatingLogLiveData) { eatingLog ->
        eatingLog?.let {
            if (eatingLogHelper.isEatingLogFinished(eatingLog)) {
                getElapsedRealTimeSinceBaseInMillis(eatingLog.endTime)
            } else {
                getElapsedRealTimeSinceBaseInMillis(eatingLog.startTime)
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