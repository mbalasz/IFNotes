package com.example.mateusz.ifnotes.model

import android.app.Application
import androidx.lifecycle.LiveData
import io.reactivex.Flowable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async

class Repository(application: Application) {
    val iFNotesDatabase: IFNotesDatabase = IFNotesDatabase.getDatabase(application)
    val eatingLogDao: EatingLogDao = iFNotesDatabase.eatingLogDao()

    fun getEatingLogs(): LiveData<List<EatingLog>> {
        return iFNotesDatabase.eatingLogDao().getEatingLogs()
    }

    fun updateEatingLog(eatingLog: EatingLog) {
        async(CommonPool) {
            iFNotesDatabase.eatingLogDao().update(eatingLog)
        }
    }

    fun getMostRecentEatingLog(): Flowable<EatingLog> {
        return iFNotesDatabase.eatingLogDao().getMostRecentEatingLog()
    }

    fun insertEatingLog(eatingLog: EatingLog) {
        async(CommonPool) {
            iFNotesDatabase.eatingLogDao().insert(eatingLog)
        }
    }

    fun deleteAll() {
        async(CommonPool) {
            iFNotesDatabase.eatingLogDao().deleteAll()
        }
    }
}