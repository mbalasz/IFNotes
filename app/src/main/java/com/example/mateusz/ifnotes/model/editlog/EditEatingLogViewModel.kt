package com.example.mateusz.ifnotes.model.editlog

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.model.EatingLog
import com.example.mateusz.ifnotes.model.Repository
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.util.Calendar

class EditEatingLogViewModel(application: Application): AndroidViewModel(application) {
    companion object {
        const val EXTRA_LOG_TIME_ID = "LOG_TIME_ID"
    }

    enum class EditMode {
        FIRST_MEAL,
        LAST_MEAL,
        NONE
    }

    // TODO: Implement validation logic in the repository.
    private val repository = Repository(application)
    private var editMode = EditMode.NONE
    private lateinit var firstMealDate: Calendar
    private lateinit var lastMealDate: Calendar
    private lateinit var eatingLog: EatingLog
    private val _firstMealLogTimeObservable = MutableLiveData<Long>()
    private val _lastMealLogTimeObservable = MutableLiveData<Long>()
    private val _showDateTimeDialogFragment = MutableLiveData<Event<Unit>>()

    val firstMealLogTimeObservable: LiveData<Long>
        get() = _firstMealLogTimeObservable
    val lastMealLogTimeObservable: LiveData<Long>
        get() = _lastMealLogTimeObservable
    val showDateTimeDialogFragment: LiveData<Event<Unit>>
        get() = _showDateTimeDialogFragment

    fun onEditFirstMealButtonClicked() {
        editMode = EditMode.FIRST_MEAL
        _showDateTimeDialogFragment.value = Event(Unit)
    }

    fun onEditLastMealButtonClicked() {
        editMode = EditMode.LAST_MEAL
        _showDateTimeDialogFragment.value = Event(Unit)
    }

    fun onEatingLogEdited(hour: Int, minute: Int) {
        if (editMode == EditMode.FIRST_MEAL) {
            _firstMealLogTimeObservable.value =
                    DateTimeUtils.timeToMillis(firstMealDate, hour, minute)
        } else if (editMode == EditMode.LAST_MEAL) {
            _lastMealLogTimeObservable.value =
                    DateTimeUtils.timeToMillis(lastMealDate, hour, minute)
        }
        editMode = EditMode.NONE
    }

    fun onActivityCreated(intent: Intent?) {
        intent?.extras?.let {
            async(UI) {
                val eatingLogDeferred = async { repository.getEatingLog(it[EXTRA_LOG_TIME_ID] as Int) }
                eatingLog = eatingLogDeferred.await()
                val firstMealLogTime = eatingLog.startTime
                firstMealDate = Calendar.getInstance()
                firstMealDate.timeInMillis = firstMealLogTime
                val lastMealLogTime = eatingLog.endTime
                lastMealDate = Calendar.getInstance()
                lastMealDate.timeInMillis = lastMealLogTime
                _firstMealLogTimeObservable.value = firstMealLogTime
                _lastMealLogTimeObservable.value = lastMealLogTime
            }
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