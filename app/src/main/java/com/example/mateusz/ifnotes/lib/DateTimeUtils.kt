package com.example.mateusz.ifnotes.lib

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateTimeUtils {
    companion object {
        private val defaultDateTimeFormat = SimpleDateFormat("${getDateFormat()} ${getTimeFormat()}", Locale.ENGLISH)
        private val defaultDateFormat = SimpleDateFormat("${getDateFormat()}", Locale.ENGLISH)

        fun getDateFormat(): String = "dd/MM/yyyy"

        fun getTimeFormat(): String = "HH:mm"

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
            val cal = getCalendar()
            cal.timeInMillis = timeInMillis
            return cal.get(Calendar.MINUTE)
        }

        fun getHourFromMillis(timeInMillis: Long): Int {
            val cal = getCalendar()
            cal.timeInMillis = timeInMillis
            return cal.get(Calendar.HOUR_OF_DAY)
        }

        fun getYearFromMillis(timeInMillis: Long): Int {
            val cal = getCalendar()
            cal.timeInMillis = timeInMillis
            return cal.get(Calendar.YEAR)
        }

        fun getMonthFromMillis(timeInMillis: Long): Int {
            val cal = getCalendar()
            cal.timeInMillis = timeInMillis
            return cal.get(Calendar.MONTH)
        }

        fun getDayOfMonthFromMillis(timeInMillis: Long): Int {
            val cal = getCalendar()
            cal.timeInMillis = timeInMillis
            return cal.get(Calendar.DAY_OF_MONTH)
        }

        fun parseDateTime(dateTime: String, dateTimeFormat: String): Long? {
            return try {
                val date = SimpleDateFormat(dateTimeFormat, Locale.ENGLISH).parse(dateTime)
                val calendar = getCalendar()
                calendar.time = date
                calendar.timeInMillis
            } catch (exception: ParseException) {
                null
            }
        }

        fun dateTimeToMillis(day: Int, month: Int, year: Int, hour: Int, minute: Int): Long {
            val calendar = getCalendar()
            calendar.set(
                year,
                month,
                day,
                hour,
                minute,
                0)
            return calendar.timeInMillis
        }

        /**
         * Converts given time to milliseconds. It uses the current date.
         */
        fun timeToMillis(hour: Int, minute: Int): Long {
            val logTime = getCalendar()
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
            val logTime = getCalendar()
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
        
        private fun getCalendar(): Calendar {
            return Calendar.getInstance(Locale.ENGLISH)
        }
    }
}
