package com.example.mateusz.ifnotes.presentation.ifnotes

import android.app.Application
import android.content.Intent
import android.graphics.Color
import androidx.lifecycle.*
import com.example.mateusz.ifnotes.chart.ui.EatingLogsChartActivity
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.domain.EatingLogValidator
import com.example.mateusz.ifnotes.domain.entity.LogDate
import com.example.mateusz.ifnotes.domain.usecases.LogMostRecentMeal
import com.example.mateusz.ifnotes.domain.usecases.ObserveMostRecentEatingLog
import com.example.mateusz.ifnotes.eatinglogs.ui.EatingLogsActivity
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.lib.SystemClockWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Clock
import java.util.*
import javax.inject.Inject

class IFNotesViewModel @Inject constructor(
    application: Application,
    private val clock: Clock,
    private val systemClock: SystemClockWrapper,
    private val eatingLogValidator: EatingLogValidator,
    @MainScope mainScope: CoroutineScope,
    private val observeMostRecentEatingLog: ObserveMostRecentEatingLog,
    private val logMostRecentMeal: LogMostRecentMeal
) : AndroidViewModel(application), CoroutineScope by mainScope {
    companion object {
        val DARK_GREEN = Color.parseColor("#a4c639")
        val DARK_RED = Color.parseColor("#8b0000")

        const val SHORT_TIME_MS = 900_000L
        const val MID_TIME_MS = 1_800_000L
        const val LONG_TIME_MS = 3_600_000L
    }

    enum class LogState {
        FIRST_MEAL,
        LAST_MEAL,
        NO_CURRENT_LOG
    }

    data class TimeSinceLastActivityChronometerData(val baseTime: Long, val color: Int)

    data class LogTimeValidationMessage(val message: String)

    data class EatingLogDisplay(val logState: LogState, val logTime: String)

    private val currentEatingLogLiveData by lazy {
        LiveDataReactiveStreams.fromPublisher(observeMostRecentEatingLog())
    }

    private val logTimeValidationMessageLiveData =
            MutableLiveData<LogTimeValidationMessage>()

    val startActivityData: LiveData<Event<Intent>>
        get() = _startActivityLiveData
    private val _startActivityLiveData = MutableLiveData<Event<Intent>>()

    val logButtonState by lazy {
        Transformations.map(currentEatingLogLiveData) { eatingLogOptional ->
            val eatingLog = eatingLogOptional.orNull()
            eatingLog?.let {
                if (eatingLogValidator.isEatingLogFinished(it)) {
                    LogState.FIRST_MEAL
                } else {
                    LogState.LAST_MEAL
                }
            } ?: run { LogState.FIRST_MEAL }
        }
    }

    val timeSinceLastActivity by lazy {
        Transformations.map(currentEatingLogLiveData) { eatingLogOptional ->
            val eatingLog = eatingLogOptional.orNull()
                eatingLog?.let {
                    it.endTime?.dateTimeInMillis?.let {
                        TimeSinceLastActivityChronometerData(
                            getElapsedRealTimeSinceBaseInMillis(it),
                            DARK_GREEN)
                    } ?: it.startTime?.dateTimeInMillis?.let {
                        TimeSinceLastActivityChronometerData(
                            getElapsedRealTimeSinceBaseInMillis(it),
                            DARK_RED)
                    }
                } ?: run { null }
        }
    }

    val currentEatingLogDisplayLiveData by lazy {
        Transformations.map(currentEatingLogLiveData) { eatingLogOptional ->
            val eatingLog = eatingLogOptional.orNull()
            eatingLog?.let {
                val simpleDateFormat =
                    SimpleDateFormat("dd/M/yyyy HH:mm:ss", Locale.ENGLISH)
                it.endTime?.dateTimeInMillis?.let {
                    EatingLogDisplay(LogState.LAST_MEAL, simpleDateFormat.format(it))
                } ?: it.startTime?.dateTimeInMillis?.let {
                    EatingLogDisplay(LogState.FIRST_MEAL, simpleDateFormat.format(it))
                }
            } ?: run { EatingLogDisplay(LogState.NO_CURRENT_LOG, "") }
        }
    }

    fun getLogTimeValidationMessageLiveData(): LiveData<LogTimeValidationMessage> {
        return logTimeValidationMessageLiveData
    }

    fun onNewManualLog(hour: Int, minute: Int) {
        val currMillis = getCurrentCalendarTime()
        val year = DateTimeUtils.getYearFromMillis(currMillis)
        val month = DateTimeUtils.getMonthFromMillis(currMillis)
        val day = DateTimeUtils.getDayOfMonthFromMillis(currMillis)
        maybeUpdateCurrentEatingLog(DateTimeUtils.dateTimeToMillis(day, month, year, hour, minute))
    }

    fun onLogButtonClicked() {
        updateCurrentEatingLog(getCurrentCalendarTime())
    }

    fun onLogShortTimeAgoClicked() {
        maybeUpdateCurrentEatingLog(getCurrentCalendarTime() - SHORT_TIME_MS)
    }

    fun onLogMidTimeAgoClicked() {
        maybeUpdateCurrentEatingLog(getCurrentCalendarTime() - MID_TIME_MS)
    }

    fun onLogLongTimeAgoClicked() {
        maybeUpdateCurrentEatingLog(getCurrentCalendarTime() - LONG_TIME_MS)
    }

    fun onHistoryButtonClicked() {
        val intent = Intent(getApplication(), EatingLogsActivity::class.java)
        _startActivityLiveData.value = Event(intent)
    }

    fun onChartButtonClicked() {
        val intent = Intent(getApplication(), EatingLogsChartActivity::class.java)
        _startActivityLiveData.value = Event(intent)
    }

    override fun onCleared() {
        super.onCleared()
        cancel()
    }

    private fun maybeUpdateCurrentEatingLog(newLogTime: Long) {
        if (validateNewLogTime(newLogTime)) {
            launch {
                updateCurrentEatingLog(newLogTime)
            }
        }
    }

    private fun validateNewLogTime(logTime: Long): Boolean {
        when (eatingLogValidator.validateNewLogTime(
                logTime, currentEatingLogLiveData.value?.orNull())) {
            EatingLogValidator.NewLogValidationStatus.SUCCESS -> Unit
            EatingLogValidator.NewLogValidationStatus.ERROR_TIME_TOO_EARLY -> {
                val validationMessage =
                    LogTimeValidationMessage(
                        message = "New log time cannot be sooner than the previous log" +
                            " time")
                logTimeValidationMessageLiveData.value = validationMessage
                return false
            }
            EatingLogValidator.NewLogValidationStatus.ERROR_TIME_IN_THE_FUTURE -> {
                val validationMessage =
                    LogTimeValidationMessage(message = "New log time cannot be in the future")
                logTimeValidationMessageLiveData.value = validationMessage
                return false
            }
        }
        return true
    }

    private fun updateCurrentEatingLog(logTime: Long) {
        launch {
            logMostRecentMeal(LogDate(logTime, ""))
        }
    }

    /**
     * Calculates how much time has passed since {@param baseInMillis} and shifts that value in
     * reference to elapsed real time.
     */
    private fun getElapsedRealTimeSinceBaseInMillis(baseInMillis: Long): Long {
        return systemClock.elapsedRealtime() - (clock.millis() - baseInMillis)
    }

    private fun getCurrentCalendarTime(): Long {
        return clock.millis()
    }
}
