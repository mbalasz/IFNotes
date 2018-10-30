package com.example.mateusz.ifnotes.model.eatinglogs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.mateusz.ifnotes.model.EatingLog
import com.example.mateusz.ifnotes.model.Repository

class EatingLogsViewModel(application: Application): AndroidViewModel(application) {
    val repository = Repository(application)

    fun getEatingLogs(): LiveData<List<EatingLog>> {
        return repository.getEatingLogs()
    }
}