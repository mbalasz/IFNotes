package com.example.mateusz.ifnotes.presentation.ifnotes.ui

import android.os.SystemClock
import android.widget.Chronometer

class LastActivityChronometerWrapper(private val delegate: Chronometer) {
    init {
        delegate.setOnChronometerTickListener {
            it.text = getFormattedText(SystemClock.elapsedRealtime() - it.base)
        }
    }

    fun setBase(base: Long) {
        delegate.base = base
    }

    fun start() {
        delegate.start()
    }

    fun setColor(color: Int) {
        delegate.setTextColor(color)
    }

    fun reset() {
        delegate.stop()
        delegate.base = SystemClock.elapsedRealtime()
    }

    fun getFormattedText(elapsedTime: Long): String {
        val hour = elapsedTime / 3600000
        val minute = elapsedTime % 3600000 / 60000
        val second = elapsedTime % 3600000 % 60000 / 1000
        val hhour = if (hour < 10) "0$hour" else "$hour"
        val mminute = if (minute < 10) "0$minute" else "$minute"
        val ssecond = if (second < 10) "0$second" else "$second"
        return "$hhour:$mminute:$ssecond"
    }
}
