package com.example.mateusz.ifnotes.domain.usecases

import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import javax.inject.Inject

class LogFirstMeal @Inject constructor(
    private val eatingLogsRepository: EatingLogsRepository) {

    // TODO: make this operation atomic
    suspend operator fun invoke(logDate: LogDate): ValidationStatus {
        val currentMostRecentEatingLog = eatingLogsRepository.getMostRecentEatingLog()
        if (currentMostRecentEatingLog != null && !currentMostRecentEatingLog.isFinished()) {
            return ValidationStatus.MOST_RECENT_EATING_LOG_NOT_FINISHED
        }

        val validationStatus = currentMostRecentEatingLog?.endTime?.dateTimeInMillis?.let {
            validateNewLogTime(logDate.dateTimeInMillis, it)
        } ?: ValidationStatus.SUCCESS

        if (validationStatus == ValidationStatus.SUCCESS) {
            eatingLogsRepository.insertEatingLog(EatingLog(startTime = logDate))
        }

        return validationStatus
    }

    private fun validateNewLogTime(firstMealLogTime: Long, prevLogLastMealLogTime: Long):
        ValidationStatus {
        if (prevLogLastMealLogTime > firstMealLogTime) {
            return ValidationStatus.ERROR_TIME_TOO_EARLY
        }
        return ValidationStatus.SUCCESS
    }

    enum class ValidationStatus {
        SUCCESS,
        ERROR_TIME_TOO_EARLY,
        MOST_RECENT_EATING_LOG_NOT_FINISHED,
    }
}
