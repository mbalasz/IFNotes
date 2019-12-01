package com.example.mateusz.ifnotes.data

import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.google.common.base.Optional
import io.reactivex.Flowable

interface EatingLogsLocalDataSource {
    fun getMostRecentEatingLog(): Flowable<Optional<EatingLog>>
}
