package com.example.mateusz.ifnotes.model

import android.app.Application
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async

class Repository(application: Application) {
    val iFNotesDatabase: IFNotesDatabase = IFNotesDatabase.getDatabase(application)
    private val eatingLogsStore = EatingLogsStore()

    init {
        async(CommonPool) {
            eatingLogsStore.setEatingLogs(
                    iFNotesDatabase.eatingLogDao().getEatingLogs().toMutableList())
        }
    }

    fun getEatingLogsObservable(): Observable<List<EatingLog>> {
        return eatingLogsStore.observe()
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
        eatingLogsStore.deleteEatingLog(eatingLog)
        async(CommonPool) {
            iFNotesDatabase.eatingLogDao().delete(eatingLog)
        }
    }

    fun insertEatingLog(eatingLog: EatingLog) {
        eatingLogsStore.insertEatingLog(eatingLog)
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