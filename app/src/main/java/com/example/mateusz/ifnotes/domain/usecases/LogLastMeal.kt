package com.example.mateusz.ifnotes.domain.usecases

import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.LogDate
import javax.inject.Inject

class LogLastMeal @Inject constructor(private val eatingLogsRepository: EatingLogsRepository,
                                      private val updateEatingLog: UpdateEatingLog) {
    suspend operator fun invoke(logDate: LogDate) : ValidationStatus {
        return eatingLogsRepository.runInTransaction {
            logLastMeal(logDate)
        }
    }

    private suspend fun logLastMeal(logDate: LogDate): ValidationStatus {
        // TODO: make sure to convert logDate to UTC before storing in the database.
        val currentMostRecentEatingLog = eatingLogsRepository.getMostRecentEatingLog()

        if (currentMostRecentEatingLog == null || currentMostRecentEatingLog.isFinished()) {
            return ValidationStatus.ERROR_MOST_RECENT_EATING_LOG_NOT_FINISHED
        }

        val validationStatus = currentMostRecentEatingLog.startTime?.dateTimeInMillis?.let {
            validateNewLogTime(it, logDate.dateTimeInMillis)
        } ?: throw IllegalStateException("EatingLog is not null but it doesn't have start time")

        if (validationStatus == ValidationStatus.SUCCESS) {
            val updatedEatingLog = currentMostRecentEatingLog.copy(endTime = logDate)
            updateEatingLog(updatedEatingLog)
        }

        return validationStatus
    }

    private fun validateNewLogTime(firstMealLogTime: Long, lastMealLogTime: Long):
        ValidationStatus {
        if (lastMealLogTime < firstMealLogTime) {
            return ValidationStatus.ERROR_LAST_MEAL_LOG_TIME_IS_EARLIER_THAN_FIRST_MEAL_LOG_TIME
        }
        return ValidationStatus.SUCCESS
    }

    enum class ValidationStatus {
        SUCCESS,
        ERROR_LAST_MEAL_LOG_TIME_IS_EARLIER_THAN_FIRST_MEAL_LOG_TIME,
        ERROR_MOST_RECENT_EATING_LOG_NOT_FINISHED
    }
}
