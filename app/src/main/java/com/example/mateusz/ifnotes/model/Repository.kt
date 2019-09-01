package com.example.mateusz.ifnotes.model

import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.lib.EatingLogValidator
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.model.data.IFNotesDatabase
import com.google.common.base.Optional
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class Repository @Inject constructor(
    private val iFNotesDatabase: IFNotesDatabase,
    private val eatingLogValidator: EatingLogValidator,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) {

    open fun getEatingLogsObservable(): Flowable<List<EatingLog>> {
        return iFNotesDatabase.eatingLogDao().getEatingLogsFlowable()
    }

    open suspend fun updateEatingLog(eatingLog: EatingLog) = withContext(ioDispatcher) {
        val status = validateUpdate(eatingLog)
        if (status == EatingLogValidator.EatingLogValidationStatus.SUCCESS) {
            iFNotesDatabase.eatingLogDao().update(eatingLog)
        }
        status
    }

    open suspend fun insertEatingLog(eatingLog: EatingLog) = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().insert(eatingLog)
    }

    open suspend fun getEatingLog(id: Int): EatingLog? = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().getEatingLog(id)
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

    open suspend fun deleteEatingLog(eatingLog: EatingLog) = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().delete(eatingLog)
    }

    open suspend fun deleteAll() = withContext(ioDispatcher) {
        iFNotesDatabase.eatingLogDao().deleteAll()
    }

    private fun validateUpdate(eatingLog: EatingLog): EatingLogValidator.EatingLogValidationStatus {
        val mutableLogs = iFNotesDatabase.eatingLogDao().getEatingLogs().toMutableList()
        val oldLog = mutableLogs.find { it.id == eatingLog.id }
        oldLog?.let {
            mutableLogs.remove(it)
        }
        return eatingLogValidator.validateNewEatingLog(eatingLog, mutableLogs)
    }
}
