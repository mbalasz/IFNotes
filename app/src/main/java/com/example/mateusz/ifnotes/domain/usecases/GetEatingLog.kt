package com.example.mateusz.ifnotes.domain.usecases

import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import javax.inject.Inject

class GetEatingLog @Inject constructor(private val eatingLogsRepository: EatingLogsRepository) {
    suspend operator fun invoke(eatingLogId: Int): EatingLog? {
        return eatingLogsRepository.getEatingLog(eatingLogId)
    }
}
