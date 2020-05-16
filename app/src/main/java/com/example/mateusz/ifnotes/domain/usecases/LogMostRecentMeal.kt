package com.example.mateusz.ifnotes.domain.usecases

import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import javax.inject.Inject

class LogMostRecentMeal @Inject constructor(
    private val logFirstMeal: LogFirstMeal,
    private val logLastMeal: LogLastMeal,
    private val eatingLogsRepository: EatingLogsRepository) {

    suspend operator fun invoke(logDate: LogDate) {
        eatingLogsRepository.runInTransaction {
            logMostRecentMeal(logDate)
        }
    }

    private suspend fun logMostRecentMeal(logDate: LogDate) {
        // TODO: make sure to convert logDate to UTC before storing in the database.
        val mostRecentEatingLog = eatingLogsRepository.getMostRecentEatingLog()
        val logTimeValidationStatus =
            validateNewLogTime(logDate.dateTimeInMillis, mostRecentEatingLog)
        if (logTimeValidationStatus != ValidationStatus.SUCCESS) {
            throw IllegalStateException(
                "Attempt to update most recent eating log with an invalid logTime:" +
                    " $logTimeValidationStatus")
        }
        if (mostRecentEatingLog == null || mostRecentEatingLog.isFinished()) {
            logFirstMeal(logDate)
        } else {
            logLastMeal(logDate)
        }
    }

    private fun validateNewLogTime(logTime: Long, currentMostRecentEatingLog: EatingLog?): ValidationStatus {
        if (currentMostRecentEatingLog == null) {
            return ValidationStatus.SUCCESS
        }
        currentMostRecentEatingLog.endTime?.dateTimeInMillis?.let {
            if (it > logTime) {
                return ValidationStatus.ERROR_TIME_TOO_EARLY
            }
        } ?: currentMostRecentEatingLog.startTime?.dateTimeInMillis?.let {
            if (it > logTime) {
                return ValidationStatus.ERROR_TIME_TOO_EARLY
            }
        }
        return ValidationStatus.SUCCESS
    }


    enum class ValidationStatus {
        SUCCESS,
        ERROR_TIME_TOO_EARLY
    }
}
