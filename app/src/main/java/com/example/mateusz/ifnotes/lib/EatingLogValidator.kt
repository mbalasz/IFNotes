package com.example.mateusz.ifnotes.lib

import com.example.mateusz.ifnotes.model.data.EatingLog
import java.lang.IllegalStateException
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EatingLogValidator @Inject constructor(private val clock: Clock) {

    enum class NewLogTimeValidationStatus {
        SUCCESS,
        ERROR_TIME_TOO_EARLY,
        ERROR_TIME_IN_THE_FUTURE
    }

    enum class EatingLogValidationStatus {
        SUCCESS,
        START_TIME_TOO_EARLY,
        ERROR_START_TIME_IN_THE_FUTURE,
        ERROR_END_TIME_IN_THE_FUTURE,
        START_TIME_LATER_THAN_END_TIME,
        END_TIME_TOO_LATE,
        NO_START_TIME,
    }

    fun validateNewLogTime(logTime: Long, currentEatingLog: EatingLog?): NewLogTimeValidationStatus {
        if (logTime > clock.millis()) {
            return NewLogTimeValidationStatus.ERROR_TIME_IN_THE_FUTURE
        }
        if (currentEatingLog == null) {
            return NewLogTimeValidationStatus.SUCCESS
        }
        currentEatingLog.endTime?.dateTimeInMillis?.let {
            if (it > logTime) {
                return NewLogTimeValidationStatus.ERROR_TIME_TOO_EARLY
            }
        } ?: currentEatingLog.startTime?.dateTimeInMillis?.let {
            if (it > logTime) {
                return NewLogTimeValidationStatus.ERROR_TIME_TOO_EARLY
            }
        }
        return NewLogTimeValidationStatus.SUCCESS
    }

    open fun validateNewEatingLog(newEatingLog: EatingLog, eatingLogs: List<EatingLog>):
            EatingLogValidationStatus {
        if (newEatingLog.startTime == null) {
            return EatingLogValidationStatus.NO_START_TIME
        }
        if (newEatingLog.startTime.dateTimeInMillis > clock.millis()) {
            return EatingLogValidationStatus.ERROR_START_TIME_IN_THE_FUTURE
        }
        newEatingLog.endTime?.dateTimeInMillis?.let {
            if (newEatingLog.startTime.dateTimeInMillis > it) {
                return EatingLogValidationStatus.START_TIME_LATER_THAN_END_TIME
            }
            if (it > clock.millis()) {
                return EatingLogValidationStatus.ERROR_END_TIME_IN_THE_FUTURE
            }
        }
        val sortedEatingLogs = eatingLogs.sortedWith(compareBy(
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
        if (eatingLog.endTime == null || nextEatingLog.startTime == null) {
            return false
        }
        return eatingLog.endTime.dateTimeInMillis < nextEatingLog.startTime.dateTimeInMillis
    }

    fun isEatingLogFinished(eatingLog: EatingLog): Boolean {
        return eatingLog.startTime != null && eatingLog.endTime != null
    }
}
