package com.example.mateusz.ifnotes.lib

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateTimeUtils {
    companion object {
        private val defaultDateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        private val defaultDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

        fun toDateTimeString(timeInMillis: Long): String {
            return toDateTimeString(timeInMillis, defaultDateTimeFormat)
        }

        fun toDateTimeString(timeInMillis: Long, dateTimeFormat: SimpleDateFormat): String {
            return dateTimeFormat.format(timeInMillis)
        }

        fun toDateTimeString(day: Int, month: Int, year: Int, hour: Int, minute: Int): String {
            return defaultDateTimeFormat.format(dateTimeToMillis(day, month, year, hour, minute))
        }

        fun toDateString(timeInMillis: Long): String {
            return defaultDateFormat.format(timeInMillis)
        }

        fun getMinuteFromMillis(timeInMillis: Long): Int {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timeInMillis
            return cal.get(Calendar.MINUTE)
        }

        fun getHourFromMillis(timeInMillis: Long): Int {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timeInMillis
            return cal.get(Calendar.HOUR_OF_DAY)
        }

        fun getYearFromMillis(timeInMillis: Long): Int {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timeInMillis
            return cal.get(Calendar.YEAR)
        }

        fun getMonthFromMillis(timeInMillis: Long): Int {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timeInMillis
            return cal.get(Calendar.MONTH)
        }

        fun getDayOfMonthFromMillis(timeInMillis: Long): Int {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timeInMillis
            return cal.get(Calendar.DAY_OF_MONTH)
        }

        fun dateTimeToMillis(day: Int, month: Int, year: Int, hour: Int, minute: Int): Long {
            val calendar = Calendar.getInstance()
            calendar.set(
                year,
                month,
                day,
                hour,
                minute)
            return calendar.timeInMillis
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

        fun isEqualToDate(millis: Long, day: Int, month: Int, year: Int, hour: Int, minute: Int): Boolean {
            return getYearFromMillis(millis) == year
                && getMonthFromMillis(millis) == month
                && getDayOfMonthFromMillis(millis) == day
                && getHourFromMillis(millis) == hour
                && getMinuteFromMillis(millis) == minute
        }
    }
}
