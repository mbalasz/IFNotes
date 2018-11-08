package com.example.mateusz.ifnotes.model.ifnotes

import com.example.mateusz.ifnotes.model.EatingLog

class EatingLogHelper {

    enum class LogTimeValidationStatus {
        SUCCESS,
        ERROR_TIME_TOO_EARLY,
        ERROR_TIME_IN_THE_FUTURE
    }

    fun validateNewLogTime(logTime: Long, currentEatingLog: EatingLog?): LogTimeValidationStatus {
        if (logTime > System.currentTimeMillis()) {
            return LogTimeValidationStatus.ERROR_TIME_IN_THE_FUTURE
        }
        if (currentEatingLog == null) {
            return LogTimeValidationStatus.SUCCESS
        }
        if (isEatingLogFinished(currentEatingLog)) {
            if (currentEatingLog.endTime > logTime) {
                return LogTimeValidationStatus.ERROR_TIME_TOO_EARLY
            }
        } else {
            if (currentEatingLog.startTime > logTime) {
                return LogTimeValidationStatus.ERROR_TIME_TOO_EARLY
            }
        }
        return LogTimeValidationStatus.SUCCESS
    }


    fun isEatingLogFinished(eatingLog: EatingLog?): Boolean {
        return eatingLog == null || eatingLog.startTime != 0L && eatingLog.endTime != 0L
    }
}