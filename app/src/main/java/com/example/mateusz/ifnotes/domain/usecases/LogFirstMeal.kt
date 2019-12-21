package com.example.mateusz.ifnotes.domain.usecases

import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import javax.inject.Inject

class LogFirstMeal @Inject constructor(private val eatingLogsRepository: EatingLogsRepository) {

    suspend operator fun invoke(logDate: LogDate): ValidationStatus {
        return eatingLogsRepository.runInTransaction {
            logFirstMeal(logDate)
        }
    }

    private suspend fun logFirstMeal(logDate: LogDate): ValidationStatus {
        val currentMostRecentEatingLog = eatingLogsRepository.getMostRecentEatingLog()
        if (currentMostRecentEatingLog != null && !currentMostRecentEatingLog.isFinished()) {
            return ValidationStatus.ERROR_MOST_RECENT_EATING_LOG_NOT_FINISHED
        }

        val validationStatus = currentMostRecentEatingLog?.let {eatingLog ->
            eatingLog.endTime?.dateTimeInMillis?.let {
                validateNewLogTime(logDate.dateTimeInMillis, it)
            } ?: throw IllegalStateException("EatingLog is not finished")
        } ?: ValidationStatus.SUCCESS

        if (validationStatus == ValidationStatus.SUCCESS) {
            eatingLogsRepository.insertEatingLog(EatingLog(startTime = logDate))
        }

        return validationStatus
    }

    private fun validateNewLogTime(firstMealLogTime: Long, prevLogLastMealTime: Long):
        ValidationStatus {
        if (prevLogLastMealTime > firstMealLogTime) {
            return ValidationStatus.ERROR_FIRST_MEAL_LOG_TIME_IS_EARLIER_THAN_LAST_MEAL_LOG_TIME
        }
        return ValidationStatus.SUCCESS
    }

    enum class ValidationStatus {
        SUCCESS,
        ERROR_FIRST_MEAL_LOG_TIME_IS_EARLIER_THAN_LAST_MEAL_LOG_TIME,
        ERROR_MOST_RECENT_EATING_LOG_NOT_FINISHED,
    }
}
