package com.example.mateusz.ifnotes.domain.usecases

import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import io.reactivex.Flowable
import javax.inject.Inject

class ObserveEatingLogs @Inject constructor(
    private val eatingLogsRepository: EatingLogsRepository) {
    operator fun invoke(): Flowable<List<EatingLog>> {
        return eatingLogsRepository.observeEatingLogs()
    }
}
