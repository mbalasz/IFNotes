package com.example.mateusz.ifnotes.model

import android.app.Application
import com.example.mateusz.ifnotes.lib.EatingLogValidator
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.model.data.IFNotesDatabase
import com.google.common.base.Optional
import io.reactivex.Flowable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

class Repository(application: Application) {
    private val iFNotesDatabase: IFNotesDatabase = IFNotesDatabase.getDatabase(application)
    private val eatingLogValidator = EatingLogValidator()

    fun getEatingLogsObservable(): Flowable<List<EatingLog>> {
        return iFNotesDatabase.eatingLogDao().getEatingLogsFlowable()
    }

    fun updateEatingLog(eatingLog: EatingLog):
            Deferred<EatingLogValidator.EatingLogValidationStatus> {
        return async(CommonPool) {
            val status = validateUpdate(eatingLog)
            if (status == EatingLogValidator.EatingLogValidationStatus.SUCCESS) {
                iFNotesDatabase.eatingLogDao().update(eatingLog)
            }
            status
        }
    }

    private fun validateUpdate(eatingLog: EatingLog): EatingLogValidator.EatingLogValidationStatus {
        val mutableLogs = iFNotesDatabase.eatingLogDao().getEatingLogs().toMutableList()
        val oldLog = mutableLogs.find { it.id == eatingLog.id }
        oldLog?.let {
            mutableLogs.remove(it)
        }
        return eatingLogValidator.validateNewEatingLog(eatingLog, mutableLogs)
    }

    fun insertEatingLog(eatingLog: EatingLog) {
        async(CommonPool) {
            iFNotesDatabase.eatingLogDao().insert(eatingLog)
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

    fun deleteAll(): Deferred<Unit> {
        return async(CommonPool) {
            iFNotesDatabase.eatingLogDao().deleteAll()
        }
    }
}