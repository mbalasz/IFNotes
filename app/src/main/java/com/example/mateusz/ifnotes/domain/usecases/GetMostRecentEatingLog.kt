package com.example.mateusz.ifnotes.domain.usecases

import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import io.reactivex.Flowable
import com.google.common.base.Optional
import javax.inject.Inject

class GetMostRecentEatingLog @Inject constructor(
    private val eatingLogsRepository: EatingLogsRepository) {
    operator fun invoke(): Flowable<Optional<EatingLog>> {
        return eatingLogsRepository.getMostRecentEatingLog()
    }
}
