package com.example.mateusz.ifnotes.model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.RoomDatabase
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import java.util.Date

class Repository(application: Application) {
    val iFNotesDatabase: IFNotesDatabase = IFNotesDatabase.getDatabase(application)
    val eatingLogDao: EatingLogDao = iFNotesDatabase.eatingLogDao()

    fun getEatingLogs(): List<EatingLog> {
        return iFNotesDatabase.eatingLogDao().getEatingLogs()
    }

    fun getEatingLogByDate(date: Date): EatingLog {
        return iFNotesDatabase.eatingLogDao().getEatingLogByDate(date.time)
    }

    fun getMostRecentEatingLog(): LiveData<EatingLog> {
        return iFNotesDatabase.eatingLogDao().getMostRecentEatingLog()
    }

    fun insertEatingLog(eatingLog: EatingLog) {
        async(CommonPool) {
            iFNotesDatabase.eatingLogDao().insert(eatingLog)
        }
    }
}