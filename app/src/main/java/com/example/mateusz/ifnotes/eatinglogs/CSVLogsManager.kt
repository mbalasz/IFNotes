package com.example.mateusz.ifnotes.eatinglogs

import android.content.Context
import android.net.Uri
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.model.data.EatingLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.IllegalStateException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CSVLogsManager @Inject constructor(
    private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher) {

    companion object {
        private const val FIRST_MEAL_DATE_IDX = 0
        private const val FIRST_MEAL_TIME_IDX = 1
        private const val LAST_MEAL_DATE_IDX = 2
        private const val LAST_MEAL_TIME_IDX = 3
    }

    open suspend fun getEatingLogsFromCsv(uri: Uri): List<EatingLog> = withContext(ioDispatcher) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

        // Read headers
        bufferedReader.readLine()

        val eatingLogs = mutableListOf<EatingLog>()
        var line = bufferedReader.readLine()
        while (line != null) {
            if (line.isNotEmpty()) {
                val eatingLog = maybeCreateEatingLogFromLine(line)
                eatingLog?.let { eatingLogs.add(it) }
                    ?: throw IllegalStateException("Can't parse $line while importing csv file. " +
                        "Invalid format for eating log")
            }
            line = bufferedReader.readLine()
        }
        eatingLogs
    }

    open fun createCsvFromEatingLogs(eatingLogs: List<EatingLog>): String {
        val csvLogsBuilder = StringBuilder()
        val csvDateTimeFormat = SimpleDateFormat("${getDateFormat()},${getTimeFormat()}", Locale.ENGLISH)
        csvLogsBuilder.appendln("Start date,Start time,End date,End time")
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
            val startTime = parseDateTime("$firstMealDate $firstMealTime") ?: return null
            var endTime: Date? = null
            if (tokens.size >= 4) {
                val lastMealDate = tokens[LAST_MEAL_DATE_IDX].replace('/', '-')
                val lastMealTime = tokens[LAST_MEAL_TIME_IDX]
                endTime = parseDateTime("$lastMealDate $lastMealTime") ?: return null
            }
            if (endTime != null) {
                return EatingLog(startTime = startTime.time, endTime = endTime.time)
            }
            return EatingLog(startTime = startTime.time)
        }
        return null
    }

    private fun parseDateTime(dateTime: String): Date? {
        val dateFormatter = SimpleDateFormat(
                "${getDateFormat()} ${getTimeFormat()}", Locale.ENGLISH)
        dateFormatter.isLenient = false
        return try {
            dateFormatter.parse(dateTime)
        } catch (exception: ParseException) {
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
