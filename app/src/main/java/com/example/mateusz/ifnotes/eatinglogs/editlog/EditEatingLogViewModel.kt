package com.example.mateusz.ifnotes.eatinglogs.editlog

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mateusz.ifnotes.time.DateDialogFragment
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.model.Repository
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.util.Calendar
import javax.inject.Inject

class EditEatingLogViewModel @Inject constructor(application: Application, private val repository: Repository): AndroidViewModel(application) {
    companion object {
        const val EXTRA_LOG_TIME_ID = "LOG_TIME_ID"
    }

    enum class EditMode {
        FIRST_MEAL,
        LAST_MEAL,
        NONE
    }

    // TODO: Implement validation logic in the repository.
    private var editMode = EditMode.NONE
    private lateinit var eatingLog: EatingLog
    private val _firstMealLogTimeObservable = MutableLiveData<Long>()
    private val _lastMealLogTimeObservable = MutableLiveData<Long>()
    private val _showTimeDialogFragment = MutableLiveData<Event<Unit>>()
    private val _showDateDialogFragment = MutableLiveData<Event<Bundle?>>()

    val firstMealLogTimeObservable: LiveData<Long>
        get() = _firstMealLogTimeObservable
    val lastMealLogTimeObservable: LiveData<Long>
        get() = _lastMealLogTimeObservable
    val showTimeDialogFragment: LiveData<Event<Unit>>
        get() = _showTimeDialogFragment
    val showDateDialogFragment: LiveData<Event<Bundle?>>
        get() = _showDateDialogFragment

    fun onEditFirstMealTimeButtonClicked() {
        editMode = EditMode.FIRST_MEAL
        _showTimeDialogFragment.value = Event(Unit)
    }

    fun onEditLastMealTimeButtonClicked() {
        editMode = EditMode.LAST_MEAL
        _showTimeDialogFragment.value = Event(Unit)
    }

    fun onEditFirstMealDateButtonClicked() {
        editMode = EditMode.FIRST_MEAL
        var firstMealDateBundle: Bundle? = null
        _firstMealLogTimeObservable.value?.let {
            firstMealDateBundle = Bundle().apply {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it
                putInt(DateDialogFragment.DATE_INIT_YEAR, cal.get(Calendar.YEAR))
                putInt(DateDialogFragment.DATE_INIT_MONTH, cal.get(Calendar.MONTH))
                putInt(DateDialogFragment.DATE_INIT_DAY, cal.get(Calendar.DAY_OF_MONTH))
            }
        }
        _showDateDialogFragment.value = Event(firstMealDateBundle)
    }

    fun onEditLastMealDateButtonClicked() {
        editMode = EditMode.LAST_MEAL
        var lastMealDateBundle: Bundle? = null
        _lastMealLogTimeObservable.value?.let {
            lastMealDateBundle = Bundle().apply {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it
                putInt(DateDialogFragment.DATE_INIT_YEAR, cal.get(Calendar.YEAR))
                putInt(DateDialogFragment.DATE_INIT_MONTH, cal.get(Calendar.MONTH))
                putInt(DateDialogFragment.DATE_INIT_DAY, cal.get(Calendar.DAY_OF_MONTH))
            }
        }
        _showDateDialogFragment.value = Event(lastMealDateBundle)
    }

    fun onEatingLogEdited(hour: Int, minute: Int) {
        if (editMode == EditMode.FIRST_MEAL) {
            _firstMealLogTimeObservable.value =
                    timeInMillis(hour, minute, _firstMealLogTimeObservable.value)
        } else if (editMode == EditMode.LAST_MEAL) {
            _lastMealLogTimeObservable.value =
                    timeInMillis(hour, minute, _lastMealLogTimeObservable.value)
        }
        editMode = EditMode.NONE
    }

    fun onEatingLogEdited(day: Int, month: Int, year: Int) {
        if (editMode == EditMode.FIRST_MEAL) {
            _firstMealLogTimeObservable.value =
                    timeInMillis(day, month, year, _firstMealLogTimeObservable.value)
        } else if (editMode == EditMode.LAST_MEAL) {
            _lastMealLogTimeObservable.value =
                    timeInMillis(day, month, year, _lastMealLogTimeObservable.value)
        }
        editMode = EditMode.NONE
    }

    fun onActivityCreated(intent: Intent?) {
        intent?.extras?.let {
            async(UI) {
                val eatingLogDeferred = async {
                    repository.getEatingLog(it[EXTRA_LOG_TIME_ID] as Int)
                }
                eatingLog = eatingLogDeferred.await()
                if (eatingLog.startTime > 0L) {
                    _firstMealLogTimeObservable.value = eatingLog.startTime
                }
                if (eatingLog.endTime > 0L) {
                    _lastMealLogTimeObservable.value = eatingLog.endTime
                }
            }
        }
    }

    private fun timeInMillis(hour: Int, minute: Int, originalTimeMillis: Long?): Long {
        originalTimeMillis?.let {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it
            return DateTimeUtils.timeToMillis(cal, hour, minute)
        } ?: run {
            return DateTimeUtils.timeToMillis(hour, minute)
        }
    }

    private fun timeInMillis(day: Int, month: Int, year: Int, originalTimeMillis: Long?): Long {
        originalTimeMillis?.let {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it
            return DateTimeUtils.timeToMillis(day, month, year, cal)
        } ?: run {
            return DateTimeUtils.timeToMillis(day, month, year)
        }
    }

    fun onSaveButtonClicked() {
        val updatedEatingLog = eatingLog.copy(
                startTime = _firstMealLogTimeObservable.value!!,
                endTime = _lastMealLogTimeObservable.value!!)
        repository.updateEatingLog(updatedEatingLog)
    }

    fun onDiscardButtonClicked() {

    }
}