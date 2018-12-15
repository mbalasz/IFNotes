package com.example.mateusz.ifnotes.model

import android.app.Application
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

class Repository(application: Application) {
    val iFNotesDatabase: IFNotesDatabase = IFNotesDatabase.getDatabase(application)

    fun getEatingLogsObservable(): Flowable<List<EatingLog>> {
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

    fun deleteEatingLog(eatingLog: EatingLog) {
        async(CommonPool) {
            iFNotesDatabase.eatingLogDao().delete(eatingLog)
        }
    }

    fun insertEatingLog(eatingLog: EatingLog) {
        async(CommonPool) {
            iFNotesDatabase.eatingLogDao().insert(eatingLog)
        }
    }

    fun deleteAll(): Deferred<Unit> {
        return async(CommonPool) {
            iFNotesDatabase.eatingLogDao().deleteAll()
        }
    }
}