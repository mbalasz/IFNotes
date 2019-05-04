package com.example.mateusz.ifnotes.chart

import java.util.concurrent.TimeUnit

class WindowValidator(private val maxWindowHours: Long) {

    fun isTimeWindowValid(windowMs: Long): Boolean {
        return windowMs <= TimeUnit.HOURS.toMillis(maxWindowHours)
    }
}
