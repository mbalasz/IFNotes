package com.example.mateusz.ifnotes.lib

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateTimeUtils {
    companion object {
        val dateTimeFormat = SimpleDateFormat("dd/M/yyyy HH:mm:ss", Locale.ENGLISH)

        fun toDateTime(timeInMillis: Long): String {
            return dateTimeFormat.format(timeInMillis)
        }

        /**
         * Converts given time to milliseconds. It uses the current date.
         */
        fun timeToMillis(hour: Int, minute: Int): Long {
            val logTime = Calendar.getInstance()
            logTime.set(
                    logTime.get(Calendar.YEAR),
                    logTime.get(Calendar.MONTH),
                    logTime.get(Calendar.DAY_OF_MONTH),
                    hour,
                    minute)
            return logTime.timeInMillis
        }

        /**
         * Converts given time to milliseconds. It uses date from the given calendar param and the
         * time from hour and minute params.
         */
        fun timeToMillis(calendar: Calendar, hour: Int, minute: Int): Long {
            val logTime = Calendar.getInstance()
            logTime.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    hour,
                    minute)
            return logTime.timeInMillis
        }
    }
}