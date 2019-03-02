package com.example.mateusz.ifnotes.lib

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateTimeUtils {
    companion object {
        val defaultDateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val defaultDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

        fun toDateTimeString(timeInMillis: Long): String {
            return toDateTimeString(timeInMillis, defaultDateTimeFormat)
        }

        fun toDateTimeString(timeInMillis: Long, dateTimeFormat: SimpleDateFormat): String {
            return dateTimeFormat.format(timeInMillis)
        }

        fun toDateString(timeInMillis: Long): String {
            return defaultDateFormat.format(timeInMillis)
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
         * Converts given date to milliseconds. It uses current time.
         */
        fun timeToMillis(day: Int, month: Int, year: Int): Long {
            val logTime = Calendar.getInstance()
            logTime.set(
                    year,
                    month,
                    day,
                    logTime.get(Calendar.HOUR),
                    logTime.get(Calendar.MINUTE))
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

        /**
         * Converts given date to milliseconds. It uses time from the given calendar param and the
         * date from day, month and year params.
         */
        fun timeToMillis(day: Int, month: Int, year: Int, calendar: Calendar): Long {
            val logTime = Calendar.getInstance()
            logTime.set(
                    year,
                    month,
                    day,
                    calendar.get(Calendar.HOUR),
                    calendar.get(Calendar.MINUTE))
            return logTime.timeInMillis
        }
    }
}