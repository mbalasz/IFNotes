package com.example.mateusz.ifnotes.domain.usecases

import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import javax.inject.Inject

class DeleteAllEatingLogs @Inject constructor(
    private val eatingLogsRepository: EatingLogsRepository) {
    suspend operator fun invoke() {
        eatingLogsRepository.deleteAllEatingLogs()
    }
}
