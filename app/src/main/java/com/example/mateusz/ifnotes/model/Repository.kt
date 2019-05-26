package com.example.mateusz.ifnotes.model

import com.example.mateusz.ifnotes.lib.EatingLogValidator
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.model.data.IFNotesDatabase
import com.google.common.base.Optional
import io.reactivex.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class Repository @Inject constructor(
    private val iFNotesDatabase: IFNotesDatabase,
    private val eatingLogValidator: EatingLogValidator
) {

    fun getEatingLogsObservable(): Flowable<List<EatingLog>> {
        return iFNotesDatabase.eatingLogDao().getEatingLogsFlowable()
    }

    open suspend fun updateEatingLogAsync(eatingLog: EatingLog) = coroutineScope {
        async(Dispatchers.Default) {
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

    open suspend fun insertEatingLog(eatingLog: EatingLog) = coroutineScope {
        launch(Dispatchers.Default) {
            iFNotesDatabase.eatingLogDao().insert(eatingLog)
        }
    }

    suspend fun getEatingLog(id: Int): EatingLog? = coroutineScope {
        withContext(Dispatchers.Default) {
            iFNotesDatabase.eatingLogDao().getEatingLog(id)
        }
    }

    open fun getMostRecentEatingLog(): Flowable<Optional<EatingLog>> {
        return iFNotesDatabase.eatingLogDao().getMostRecentEatingLog().map { list ->
            if (list.isNotEmpty()) {
                Optional.of(list[0])
            } else {
                Optional.absent()
            }
        }
    }

    suspend fun deleteEatingLog(eatingLog: EatingLog) = coroutineScope {
        async(Dispatchers.Default) {
            iFNotesDatabase.eatingLogDao().delete(eatingLog)
        }
    }

    suspend fun deleteAll() = coroutineScope {
        async(Dispatchers.Default) {
            iFNotesDatabase.eatingLogDao().deleteAll()
        }
    }
}
