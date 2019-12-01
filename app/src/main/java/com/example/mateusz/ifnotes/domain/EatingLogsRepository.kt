package com.example.mateusz.ifnotes.domain

import com.example.mateusz.ifnotes.domain.entity.EatingLog
import io.reactivex.Flowable
import com.google.common.base.Optional

interface EatingLogsRepository {
    // TODO: replace Flowable with Observable. There shouldn't be a need for backpressure strategy.
    // mostRecentEatingLog changes infrequently.
    fun getMostRecentEatingLog(): Flowable<Optional<EatingLog>>
}
