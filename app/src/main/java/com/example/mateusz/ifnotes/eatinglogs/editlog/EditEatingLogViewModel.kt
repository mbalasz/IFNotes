package com.example.mateusz.ifnotes.eatinglogs.editlog

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.model.data.LogDate
import com.example.mateusz.ifnotes.time.DateDialogFragment
import com.example.mateusz.ifnotes.time.TimeDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import javax.inject.Inject

class EditEatingLogViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    @MainScope mainScope: CoroutineScope
) : AndroidViewModel(application), CoroutineScope by mainScope {
    companion object {
        const val EXTRA_EATING_LOG_ID = "EXTRA_EATING_LOG_ID"
    }

    enum class MealType {
        FIRST_MEAL,
        LAST_MEAL,
        NONE
    }

    // TODO: Implement validation logic in the repository.
    private var editMode = MealType.NONE
    private lateinit var originalEatingLog: EatingLog

    private val _showDialogFragment = MutableLiveData<Event<DialogFragment>>()
    val showDialogFragment: LiveData<Event<DialogFragment>>
        get() = _showDialogFragment

    private val _finishActivity = MutableLiveData<Event<Unit>>()
    val finishActivity: LiveData<Event<Unit>>
        get() = _finishActivity


    private val _logTimeObservables: Map<MealType, MutableLiveData<Long>>
    private val logTimeToDateStringTransformation = {
        logTimeObservable: LiveData<Long> -> Transformations.map(logTimeObservable) {
            DateTimeUtils.toDateTimeString(it)
        }
    }
    val firstMealLogTimeObservable: LiveData<String>
        get() = logTimeToDateStringTransformation.invoke(
            _logTimeObservables[MealType.FIRST_MEAL] ?:
            error("No log time for MealType.FIRST_MEAL"))
    val lastMealLogTimeObservable: LiveData<String>
        get() = logTimeToDateStringTransformation.invoke(
            _logTimeObservables[MealType.LAST_MEAL] ?:
            error("No log time for MealType.LAST_MEAL"))


    private data class EatingLogDateTime(
        val year: Int, val month: Int, val day: Int, val hour: Int = -1, val minute: Int = -1)
    private var pendingDateTimeEdit: EatingLogDateTime? = null

    init {
        _logTimeObservables =
            enumValues<MealType>()
                .asSequence()
                .map { it to MutableLiveData<Long>() }
                .toMap()
    }

    fun onEditFirstMeal() {
        showDateChooser(MealType.FIRST_MEAL)
    }

    fun onEditLastMeal() {
        showDateChooser(MealType.LAST_MEAL)
    }

    private fun showDateChooser(mealType: MealType) {
        editMode = mealType
        val dateDialogFragment = DateDialogFragment()
        _logTimeObservables[editMode]?.value?.let {
            dateDialogFragment.arguments = Bundle().apply {
                putInt(DateDialogFragment.DATE_INIT_YEAR, DateTimeUtils.getYearFromMillis(it))
                putInt(DateDialogFragment.DATE_INIT_MONTH, DateTimeUtils.getMonthFromMillis(it))
                putInt(DateDialogFragment.DATE_INIT_DAY, DateTimeUtils.getDayOfMonthFromMillis(it))
            }
        }
        _showDialogFragment.value = Event(dateDialogFragment)
    }

    fun onDateSaved(day: Int, month: Int, year: Int) {
        if (pendingDateTimeEdit != null) {
            throw IllegalStateException("Attempt to set date on existing pending log edit")
        }
        if (editMode == MealType.NONE) {
            throw IllegalStateException("Attempt to set date on an unknown meal type")
        }
        pendingDateTimeEdit = EatingLogDateTime(year, month, day)
        _showDialogFragment.value = Event(TimeDialogFragment())
    }

    fun onTimeSaved(hour: Int, minute: Int) {
        pendingDateTimeEdit?.let {
            onDateTimeSaved(it.copy(hour = hour, minute = minute))
        } ?: throw IllegalStateException("Attempt to set time on a null eatingLogDateTime")
    }

    private fun onDateTimeSaved(eatingLogDateTime: EatingLogDateTime) {
        eatingLogDateTime.apply {
            _logTimeObservables[editMode]?.value =
                DateTimeUtils.dateTimeToMillis(day, month, year, hour, minute)
        }
        resetEditMode()
    }

    private fun resetEditMode() {
        editMode = MealType.NONE
        pendingDateTimeEdit = null
    }

    fun onTimeEditCancelled() {
        resetEditMode()
    }

    fun onDateEditCancelled() {
        resetEditMode()
    }

    fun onActivityCreated(intent: Intent?) {
        intent?.extras?.let {
            launch {
                val eatingLogNullable =
                    repository.getEatingLog(it[EXTRA_EATING_LOG_ID] as Int) ?: throw RuntimeException(
                        "Attempted to obtain a non-existent log with id $EXTRA_EATING_LOG_ID")
                originalEatingLog = eatingLogNullable
                originalEatingLog.startTime?.let {
                    _logTimeObservables[MealType.FIRST_MEAL]?.value = it.dateTimeInMillis
                }
                originalEatingLog.endTime?.let {
                    _logTimeObservables[MealType.LAST_MEAL]?.value = it.dateTimeInMillis
                }
            }
        }
    }

    fun onSaveButtonClicked() {
        val startTime = _logTimeObservables[MealType.FIRST_MEAL]?.value?.let { LogDate(it, "") } ?: originalEatingLog.startTime
        val endTime = _logTimeObservables[MealType.LAST_MEAL]?.value?.let { LogDate(it, "") } ?: originalEatingLog.endTime
        val updatedEatingLog = originalEatingLog.copy(startTime = startTime, endTime = endTime)
        launch {
            repository.updateEatingLog(updatedEatingLog)
            _finishActivity.value = Event(Unit)
        }
    }

    fun onDiscardButtonClicked() {
        _finishActivity.value = Event(Unit)
    }
}
