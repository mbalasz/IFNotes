package com.example.mateusz.ifnotes.domain.usecases

import com.example.mateusz.ifnotes.domain.EatingLogValidator
import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import javax.inject.Inject

class InsertEatingLog @Inject constructor(private val eatingLogsRepository: EatingLogsRepository,
                                          private val eatingLogValidator: EatingLogValidator) {
    suspend operator fun invoke(eatingLog: EatingLog): EatingLogValidator.NewLogValidationStatus {
        return eatingLogsRepository.runInTransaction {
            val insertValidationStatus = validateInsert(eatingLog)
            if (insertValidationStatus == EatingLogValidator.NewLogValidationStatus.SUCCESS) {
                eatingLogsRepository.insertEatingLog(eatingLog)
            }
            insertValidationStatus
        }
    }

    private suspend fun validateInsert(eatingLog: EatingLog):
        EatingLogValidator.NewLogValidationStatus {
        val eatingLogs = eatingLogsRepository.getEatingLogs().toMutableList()
        return eatingLogValidator.validateNewEatingLog(eatingLog, eatingLogs)
    }
}
