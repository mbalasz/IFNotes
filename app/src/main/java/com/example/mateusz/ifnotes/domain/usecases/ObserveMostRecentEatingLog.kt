package com.example.mateusz.ifnotes.domain.usecases

import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.google.common.base.Optional
import io.reactivex.Flowable
import javax.inject.Inject

class ObserveMostRecentEatingLog @Inject constructor(
    private val eatingLogsRepository: EatingLogsRepository) {
    operator fun invoke(): Flowable<Optional<EatingLog>> {
        return eatingLogsRepository.observeMostRecentEatingLog()
    }
}
