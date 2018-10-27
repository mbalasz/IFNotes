package com.example.mateusz.ifnotes.model

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async

class EatingLogViewModel(application: Application): AndroidViewModel(application) {
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
                eatingLog.endTime
            } else {
                eatingLog.startTime
            }
        } ?: run { null }
    }

    init {
        repository.deleteAll()
        async(CommonPool) {
            currentEatingLogLiveData.value = repository.getMostRecentEatingLog()
        }
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
        val finishedEatingLog = eatingLog.copy(endTime = getCurrentTime())
        currentEatingLogLiveData.value = finishedEatingLog
        repository.updateEatingLog(finishedEatingLog)
    }

    private fun startNewEatingLog() {
        val newEatingLog = EatingLog(startTime = getCurrentTime())
        currentEatingLogLiveData.value = newEatingLog
        repository.insertEatingLog(newEatingLog)
    }

    private fun isEatingLogFinished(eatingLog: EatingLog): Boolean {
        return eatingLog.startTime != 0L && eatingLog.endTime != 0L
    }

    private fun getCurrentTime(): Long {
        return SystemClock.elapsedRealtime()
    }
}