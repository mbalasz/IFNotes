package com.example.mateusz.ifnotes.lib

import com.example.mateusz.ifnotes.model.data.EatingLog
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EatingLogValidator @Inject constructor() {

    enum class NewLogTimeValidationStatus {
        SUCCESS,
        ERROR_TIME_TOO_EARLY,
        ERROR_TIME_IN_THE_FUTURE
    }

    enum class EatingLogValidationStatus {
        SUCCESS,
        START_TIME_TOO_EARLY,
        START_TIME_LATER_THAN_END_TIME,
        END_TIME_TOO_LATE,
    }

    fun validateNewLogTime(logTime: Long, currentEatingLog: EatingLog?): NewLogTimeValidationStatus {
        if (logTime > System.currentTimeMillis()) {
            return NewLogTimeValidationStatus.ERROR_TIME_IN_THE_FUTURE
        }
        if (currentEatingLog == null) {
            return NewLogTimeValidationStatus.SUCCESS
        }
        if (isEatingLogFinished(currentEatingLog)) {
            if (currentEatingLog.endTime > logTime) {
                return NewLogTimeValidationStatus.ERROR_TIME_TOO_EARLY
            }
        } else {
            if (currentEatingLog.startTime > logTime) {
                return NewLogTimeValidationStatus.ERROR_TIME_TOO_EARLY
            }
        }
        return NewLogTimeValidationStatus.SUCCESS
    }

    open fun validateNewEatingLog(newEatingLog: EatingLog, eatingLogs: List<EatingLog>):
            EatingLogValidationStatus {
        if (newEatingLog.startTime > newEatingLog.endTime) {
            return EatingLogValidationStatus.START_TIME_LATER_THAN_END_TIME
        }
        val sortedEatingLogs = eatingLogs.sortedWith(compareBy(
                { it.startTime },
                { it.endTime }
        ))
        var idx = sortedEatingLogs.binarySearch(
                newEatingLog, compareBy<EatingLog>{ it.startTime }.thenBy { it.endTime })
        if (idx >= 0) {
            throw IllegalStateException("EatingLog shouldn't exist in the list at this stage")
        }
        idx = -idx - 1
        if (idx > 0) {
            val prevEatingLog = sortedEatingLogs[idx - 1]
            if (!validateOrder(prevEatingLog, newEatingLog)) {
                return EatingLogValidationStatus.START_TIME_TOO_EARLY
            }
        }
        if (idx < sortedEatingLogs.size) {
            val nextEatingLog = sortedEatingLogs[idx]
            if (!validateOrder(newEatingLog, nextEatingLog)) {
                return EatingLogValidationStatus.END_TIME_TOO_LATE
            }
        }
        return EatingLogValidationStatus.SUCCESS
    }

    private fun validateOrder(eatingLog: EatingLog, nextEatingLog: EatingLog): Boolean {
        return eatingLog.endTime < nextEatingLog.startTime
    }

    fun isEatingLogFinished(eatingLog: EatingLog?): Boolean {
        return eatingLog == null || eatingLog.startTime != 0L && eatingLog.endTime != 0L
    }
}