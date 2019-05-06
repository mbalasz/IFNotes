package com.example.mateusz.ifnotes.lib

import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemClockWrapper @Inject constructor() {
    fun elapsedRealtime(): Long {
        return SystemClock.elapsedRealtime()
    }
}
