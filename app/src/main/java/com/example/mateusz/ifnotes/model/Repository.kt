package com.example.mateusz.ifnotes.model

import android.app.Application
import com.example.mateusz.ifnotes.lib.EatingLogHelper
import com.google.common.base.Optional
import io.reactivex.Flowable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

class Repository(application: Application) {
    private val iFNotesDatabase: IFNotesDatabase = IFNotesDatabase.getDatabase(application)
    private val eatingLogHelper = EatingLogHelper()

    fun getEatingLogsObservable(): Flowable<List<EatingLog>> {
        return iFNotesDatabase.eatingLogDao().getEatingLogs()
    }

    fun updateEatingLog(eatingLog: EatingLog) {
        async(CommonPool) {
            iFNotesDatabase.eatingLogDao().update(eatingLog)
        }
    }

    fun getEatingLog(id: Int): EatingLog {
        return iFNotesDatabase.eatingLogDao().getEatingLog(id)
    }

    fun getMostRecentEatingLog(): Flowable<Optional<EatingLog>> {
        return iFNotesDatabase.eatingLogDao().getMostRecentEatingLog().map {list ->
            if (list.isNotEmpty()) {
                Optional.of(list[0])
            } else {
                Optional.absent()
            }
        }
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