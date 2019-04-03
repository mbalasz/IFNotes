package com.example.mateusz.ifnotes.model.eatinglogs

import android.content.Context
import android.net.Uri
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.model.EatingLog
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CSVLogsManager(private val context: Context) {
    companion object {
        private const val FIRST_MEAL_DATE_IDX = 0
        private const val FIRST_MEAL_TIME_IDX = 1
        private const val LAST_MEAL_DATE_IDX = 2
        private const val LAST_MEAL_TIME_IDX = 3
    }

    fun getEatingLogsFromCsv(uri: Uri): List<EatingLog> {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

        // Read headers
        bufferedReader.readLine()

        val eatingLogs = mutableListOf<EatingLog>()
        var line = bufferedReader.readLine()
        while (line != null) {
            val eatingLog = maybeCreateEatingLogFromLine(line)
            eatingLog?.let { eatingLogs.add(it) }
            line = bufferedReader.readLine()
        }
        return eatingLogs
    }

    fun getCsvFromEatingLogs(eatingLogs: List<EatingLog>): String {
        val csvLogsBuilder = StringBuilder()
        val csvDateTimeFormat = SimpleDateFormat("${getDateFormat()},${getTimeFormat()}", Locale.ENGLISH)
        csvLogsBuilder.append("Start date,Start time,End date, End time")
        csvLogsBuilder.appendln()
        for (eatingLog in eatingLogs) {
            val startDateTime = DateTimeUtils.toDateTimeString(eatingLog.startTime, csvDateTimeFormat)
            csvLogsBuilder.append(startDateTime)
            csvLogsBuilder.append(",")
            if (eatingLog.endTime != 0L) {
                val endDateTime = DateTimeUtils.toDateTimeString(eatingLog.endTime, csvDateTimeFormat)
                csvLogsBuilder.append(endDateTime)
            }
            csvLogsBuilder.appendln()
        }
        return csvLogsBuilder.toString()
    }

    private fun maybeCreateEatingLogFromLine(line: String): EatingLog? {
        val tokens = line.split(",")
        if (tokens.isNotEmpty() && tokens.size >= 2) {
            val firstMealDate = tokens[FIRST_MEAL_DATE_IDX].replace('/', '-')
            val firstMealTime = tokens[FIRST_MEAL_TIME_IDX]
            val startTime = parseDateTime("$firstMealDate $firstMealTime")
            var endTime: Date? = null
            if (tokens.size >= 4) {
                val lastMealDate = tokens[LAST_MEAL_DATE_IDX].replace('/', '-')
                val lastMealTime = tokens[LAST_MEAL_TIME_IDX]
                endTime = parseDateTime("$lastMealDate $lastMealTime")
            }
            if (startTime != null) {
                if (endTime != null) {
                    return EatingLog(startTime = startTime.time, endTime = endTime.time)
                }
                return EatingLog(startTime = startTime.time)
            }
        }
        return null
    }

    private fun parseDateTime(dateTime: String?): Date? {
        return if (dateTime != null) {
            val dateFormatter = SimpleDateFormat(
                    "${getDateFormat()} ${getTimeFormat()}", Locale.ENGLISH)
            dateFormatter.isLenient = false
            return try {
                dateFormatter.parse(dateTime)
            } catch (exception: ParseException) {
                null
            }
        } else {
            null
        }
    }

    private fun getDateFormat(): String {
        return "yyyy-MM-dd"
    }

    private fun getTimeFormat(): String {
        return "HH:mm"
    }
}