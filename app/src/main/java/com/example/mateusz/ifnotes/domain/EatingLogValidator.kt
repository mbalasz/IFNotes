package com.example.mateusz.ifnotes.domain

import com.example.mateusz.ifnotes.domain.entity.EatingLog
import java.lang.IllegalStateException
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EatingLogValidator @Inject constructor(private val clock: Clock) {

    enum class NewLogValidationStatus {
        SUCCESS,
        ERROR_TIME_TOO_EARLY,
        ERROR_TIME_IN_THE_FUTURE,
        START_TIME_TOO_EARLY,
        ERROR_START_TIME_IN_THE_FUTURE,
        ERROR_END_TIME_IN_THE_FUTURE,
        START_TIME_LATER_THAN_END_TIME,
        END_TIME_TOO_LATE,
        NO_START_TIME,
    }

    fun validateNewLogTime(logTime: Long, currentMostRecentEatingLog: EatingLog?): NewLogValidationStatus {
        if (logTime > clock.millis()) {
            return NewLogValidationStatus.ERROR_TIME_IN_THE_FUTURE
        }
        if (currentMostRecentEatingLog == null) {
            return NewLogValidationStatus.SUCCESS
        }
        currentMostRecentEatingLog.endTime?.dateTimeInMillis?.let {
            if (it > logTime) {
                return NewLogValidationStatus.ERROR_TIME_TOO_EARLY
            }
        } ?: currentMostRecentEatingLog.startTime?.dateTimeInMillis?.let {
            if (it > logTime) {
                return NewLogValidationStatus.ERROR_TIME_TOO_EARLY
            }
        }
        return NewLogValidationStatus.SUCCESS
    }

    open fun validateNewEatingLog(newEatingLog: EatingLog, eatingLogData: List<EatingLog>):
        NewLogValidationStatus {
        if (newEatingLog.startTime == null) {
            return NewLogValidationStatus.NO_START_TIME
        }
        if (newEatingLog.startTime.dateTimeInMillis > clock.millis()) {
            return NewLogValidationStatus.ERROR_START_TIME_IN_THE_FUTURE
        }
        newEatingLog.endTime?.dateTimeInMillis?.let {
            if (newEatingLog.startTime.dateTimeInMillis > it) {
                return NewLogValidationStatus.START_TIME_LATER_THAN_END_TIME
            }
            if (it > clock.millis()) {
                return NewLogValidationStatus.ERROR_END_TIME_IN_THE_FUTURE
            }
        }
        val sortedEatingLogs = eatingLogData.sortedWith(compareBy(
                { it.startTime?.dateTimeInMillis },
                { it.endTime?.dateTimeInMillis }
        ))
        var idx = sortedEatingLogs.binarySearch(
                newEatingLog, compareBy<EatingLog> { it.startTime?.dateTimeInMillis }.thenBy { it.endTime?.dateTimeInMillis })
        if (idx >= 0) {
            throw IllegalStateException("EatingLog shouldn't exist in the list at this stage")
        }
        idx = -idx - 1
        if (idx > 0) {
            val prevEatingLog = sortedEatingLogs[idx - 1]
            if (!validateOrder(prevEatingLog, newEatingLog)) {
                return NewLogValidationStatus.START_TIME_TOO_EARLY
            }
        }
        if (idx < sortedEatingLogs.size) {
            val nextEatingLog = sortedEatingLogs[idx]
            if (!validateOrder(newEatingLog, nextEatingLog)) {
                return NewLogValidationStatus.END_TIME_TOO_LATE
            }
        }
        return NewLogValidationStatus.SUCCESS
    }

    private fun validateOrder(eatingLogData: EatingLog, nextEatingLog: EatingLog): Boolean {
        if (eatingLogData.endTime == null || nextEatingLog.startTime == null) {
            return false
        }
        return eatingLogData.endTime.dateTimeInMillis < nextEatingLog.startTime.dateTimeInMillis
    }

    fun isEatingLogFinished(eatingLog: EatingLog): Boolean {
        return eatingLog.startTime != null && eatingLog.endTime != null
    }
}
