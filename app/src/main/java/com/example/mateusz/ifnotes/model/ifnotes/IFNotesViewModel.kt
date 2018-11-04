package com.example.mateusz.ifnotes.model.ifnotes

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.mateusz.ifnotes.model.EatingLog
import com.example.mateusz.ifnotes.model.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

class IFNotesViewModel(application: Application): AndroidViewModel(application) {
    enum class LogButtonState {
        LOG_FIRST_MEAL,
        LOG_LAST_MEAL
    }
    private val repository = Repository(application)
    private val currentEatingLogLiveData = MutableLiveData<EatingLog>()
    val logButtonState = Transformations.map(currentEatingLogLiveData) { eatingLog ->
        eatingLog?.let {
            if (isEatingLogFinished(eatingLog)) {
                LogButtonState.LOG_FIRST_MEAL
            } else {
                LogButtonState.LOG_LAST_MEAL
            }
        } ?: run { LogButtonState.LOG_FIRST_MEAL }
    }
    val timeSinceLastActivity = Transformations.map(currentEatingLogLiveData) { eatingLog ->
        eatingLog?.let {
            if (isEatingLogFinished(eatingLog)) {
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

    fun onManualLogCreated(hour: Int, minute: Int) {

    }

    fun onLogButtonClicked() {
        val currentEatingLog = currentEatingLogLiveData.value
        if (currentEatingLog == null || isEatingLogFinished(currentEatingLog)) {
            startNewEatingLog()
        } else {
            finishEatingLog(currentEatingLog)
        }
    }

    private fun finishEatingLog(eatingLog: EatingLog) {
        if (eatingLog.endTime != 0L) {
            throw IllegalStateException("Eating log was already finished while attempting to" +
                    " finish it")
        }
        val finishedEatingLog = eatingLog.copy(endTime = getCurrentCalendarTime())
        currentEatingLogLiveData.value = finishedEatingLog
        repository.updateEatingLog(finishedEatingLog)
    }

    private fun startNewEatingLog() {
        val newEatingLog = EatingLog(startTime = getCurrentCalendarTime())
        currentEatingLogLiveData.value = newEatingLog
        repository.insertEatingLog(newEatingLog)
    }

    private fun isEatingLogFinished(eatingLog: EatingLog): Boolean {
        return eatingLog.startTime != 0L && eatingLog.endTime != 0L
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