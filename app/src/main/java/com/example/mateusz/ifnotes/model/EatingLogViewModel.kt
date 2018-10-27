package com.example.mateusz.ifnotes.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

class EatingLogViewModel(application: Application): AndroidViewModel(application) {
    val repository = Repository(application)
    val mostRecentEatingLog: LiveData<EatingLog> = repository.getMostRecentEatingLog()
    private val isInEatingWindow = MutableLiveData<Boolean>()

    val eatingLogToTimeSinceLastActivity = fun(eatingLog: EatingLog?) : Long? {
        if (eatingLog == null) {
            return null
        }
        if (isEatingWindowFinished(eatingLog)) {
            // Return time since the end of last eating window.
            isInEatingWindow.value = true
            return eatingLog.endTime
        } else if (isEatingWindowInProgress(eatingLog)) {
            // Return time since the beginning of current eating window.
            return eatingLog.startTime
        }
        return null
    }
    val timeSinceLastActivity: LiveData<Long> =
            Transformations.map(mostRecentEatingLog, eatingLogToTimeSinceLastActivity)

    fun getEatingWindowLiveData(): LiveData<Boolean> {
        return isInEatingWindow
    }

    private fun isEatingWindowFinished(eatingLog: EatingLog): Boolean {
        return eatingLog.startTime != 0L && eatingLog.endTime != 0L
    }

    private fun isEatingWindowInProgress(eatingLog: EatingLog): Boolean {
        return eatingLog.startTime > 0 && eatingLog.endTime == 0L
    }
}