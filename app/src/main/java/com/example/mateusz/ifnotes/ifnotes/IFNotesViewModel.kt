package com.example.mateusz.ifnotes.ifnotes

import android.app.Application
import android.content.Intent
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.mateusz.ifnotes.chart.ui.EatingLogsChartActivity
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScheduler
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.eatinglogs.ui.EatingLogsActivity
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.domain.EatingLogValidator
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.lib.SystemClockWrapper
import com.example.mateusz.ifnotes.data.EatingLogsRepositoryImpl
import com.example.mateusz.ifnotes.data.room.EntityToDataMapper
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import com.example.mateusz.ifnotes.domain.usecases.LogFirstMeal
import com.example.mateusz.ifnotes.domain.usecases.ObserveMostRecentEatingLog
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Clock
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class IFNotesViewModel @Inject constructor(
    application: Application,
    private val eatingLogsRepositoryImpl: EatingLogsRepositoryImpl,
    private val clock: Clock,
    private val systemClock: SystemClockWrapper,
    private val eatingLogValidator: EatingLogValidator,
    @MainScope mainScope: CoroutineScope,
    @MainScheduler mainScheduler: Scheduler,
    observeMostRecentEatingLog: ObserveMostRecentEatingLog,
    private val logFirstMeal: LogFirstMeal,
    // TODO Remove this dependency. PresentationLayer should have its own representation of the
    //Eating log
    private val entityToDataMapper: EntityToDataMapper
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

    private val currentEatingLogLiveData = MutableLiveData<EatingLog>()
    private val logTimeValidationMessageLiveData =
            MutableLiveData<LogTimeValidationMessage>()

    private val currentEatingLogUpdatedChannel: Channel<Unit> = Channel()
    private val currentEatingLogDisposable: Disposable
    private var isFirstLogLoaded = false

    val startActivityData: LiveData<Event<Intent>>
        get() = _startActivityLiveData
    private val _startActivityLiveData = MutableLiveData<Event<Intent>>()

    val logButtonState = Transformations.map(currentEatingLogLiveData) { eatingLog ->
        eatingLog?.let {
            if (eatingLogValidator.isEatingLogFinished(eatingLog)) {
                LogState.FIRST_MEAL
            } else {
                LogState.LAST_MEAL
            }
        } ?: run { LogState.FIRST_MEAL }
    }
    val timeSinceLastActivity = Transformations.map(currentEatingLogLiveData) { eatingLog ->
        eatingLog?.let { currEatingLog ->
            currEatingLog.endTime?.dateTimeInMillis?.let {
                TimeSinceLastActivityChronometerData(
                        getElapsedRealTimeSinceBaseInMillis(it),
                        DARK_GREEN)
            } ?: currEatingLog.startTime?.dateTimeInMillis?.let {
                TimeSinceLastActivityChronometerData(
                        getElapsedRealTimeSinceBaseInMillis(it),
                        DARK_RED)
            }
        } ?: run { null }
    }
    val currentEatingLogDisplayLiveData =
            Transformations.map(currentEatingLogLiveData) { eatingLog ->
                eatingLog?.let {
                    val simpleDateFormat =
                            SimpleDateFormat("dd/M/yyyy HH:mm:ss", Locale.ENGLISH)
                    eatingLog.endTime?.dateTimeInMillis?.let {
                        EatingLogDisplay(
                                LogState.LAST_MEAL, simpleDateFormat.format(it))
                    } ?: eatingLog.startTime?.dateTimeInMillis?.let {
                        EatingLogDisplay(
                                LogState.FIRST_MEAL,
                                simpleDateFormat.format(it))
                    }
                } ?: run { EatingLogDisplay(LogState.NO_CURRENT_LOG, "") }
    }

    init {
        currentEatingLogDisposable = observeMostRecentEatingLog()
            .observeOn(mainScheduler)
            .subscribe ({
                if (!isFirstLogLoaded) {
                    isFirstLogLoaded = true
                }
                currentEatingLogLiveData.value = it.orNull()
                launch {
                    currentEatingLogUpdatedChannel.send(Unit)
                }
            }, {
                throw RuntimeException("Error during loading logs from database", it)
            })

        launch {
            delay(TimeUnit.SECONDS.toMillis(6))
            if (!isFirstLogLoaded) {
                throw RuntimeException("Loading logs from database timed out")
            }
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
        currentEatingLogDisposable.dispose()
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
                logTime, currentEatingLogLiveData.value)) {
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
            currentEatingLogUpdatedChannel.receive()
            val currentEatingLog = currentEatingLogLiveData.value
            val logTimeValidationStatus =
                eatingLogValidator.validateNewLogTime(logTime, currentEatingLog)
            if (logTimeValidationStatus != EatingLogValidator.NewLogValidationStatus.SUCCESS) {
                throw IllegalStateException(
                    "Attempt to update most recent eating log with an invalid logTime:" +
                        " $logTimeValidationStatus")
            }
            val newEatingLog: EatingLog
            if (currentEatingLog == null || currentEatingLog.isFinished()) {
                logFirstMeal(LogDate(logTime, ""))
            } else {
                newEatingLog = currentEatingLog.copy(endTime = LogDate(logTime, ""))
                eatingLogsRepositoryImpl.updateEatingLog(entityToDataMapper.mapFrom(newEatingLog))
            }
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
