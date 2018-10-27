package com.example.mateusz.ifnotes.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

class EatingLogViewModel(application: Application): AndroidViewModel(application) {
    val repository = Repository(application)
    val mostRecentEatingLog: LiveData<EatingLog> = repository.getMostRecentEatingLog()
    val timeSinceLastActivity: LiveData<Long> =
            Transformations.map(mostRecentEatingLog) { eatingLog ->
                if (eatingLog.startTime > 0 && eatingLog.endTime == 0L) {
                    eatingLog.startTime
                }
                else if (eatingLog.endTime > 0){
                    eatingLog.endTime
                } else {
                    null
                }}
}