package com.example.mateusz.ifnotes.lib

import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class SystemClockWrapper @Inject constructor() {
    open fun elapsedRealtime(): Long {
        return SystemClock.elapsedRealtime()
    }
}
