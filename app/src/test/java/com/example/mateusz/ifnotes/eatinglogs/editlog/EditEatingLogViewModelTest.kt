package com.example.mateusz.ifnotes.eatinglogs.editlog

import android.app.Application
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.database.IFNotesDatabaseTestModule
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import com.example.mateusz.ifnotes.domain.usecases.GetEatingLog
import com.example.mateusz.ifnotes.domain.usecases.UpdateEatingLog
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.livedata.testObserve
import com.example.mateusz.ifnotes.time.DateDialogFragment
import com.example.mateusz.ifnotes.time.TimeDialogFragment
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsInstanceOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class EditEatingLogViewModelTest {
    private val testScope = TestCoroutineScope()
    @Mock private lateinit var updateEatingLog: UpdateEatingLog
    @Mock private lateinit var getEatingLog: GetEatingLog

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Inject lateinit var editEatingLogViewModel: EditEatingLogViewModel

    @Before
    fun setUp() {
        DaggerEditEatingLogViewModelTest_TestComponent.factory().create(
            ApplicationProvider.getApplicationContext(),
            testScope,
            Dispatchers.IO,
            updateEatingLog,
            getEatingLog).inject(this)
    }

    @After
    fun cleanUp() {
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun onActivityCreated_firstMealLogDateTimeUpdated() = testScope.runBlockingTest {
        val firstMealDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 10, 20)
        val lastMealDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 18, 50)
        whenever(getEatingLog(13))
            .thenReturn(
                EatingLog(
                    startTime = LogDate(firstMealDateTime),
                    endTime = LogDate(lastMealDateTime)))
        val intent = Intent().apply {
            putExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, 13)
        }

        editEatingLogViewModel.onActivityCreated(intent)

        assertThat(
            editEatingLogViewModel.firstMealLogTimeObservable.testObserve(),
            containsString("13/04/2019 10:20"))
        assertThat(
            editEatingLogViewModel.lastMealLogTimeObservable.testObserve(),
            containsString("13/04/2019 18:50"))
    }

    @Test
    fun onEditFirstMeal_sendsDateDialogFragment() {
        editEatingLogViewModel.onEditFirstMeal()

        assertThat(
            editEatingLogViewModel.showDialogFragment.testObserve()!!.getContentIfNotHandled(),
            IsInstanceOf(DateDialogFragment::class.java))
    }

    @Test
    fun onEditFirstMeal_sendsDateDialogFragmentWithOriginalLogTime() = testScope.runBlockingTest {
        val firstMealDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 10, 20)
        whenever(getEatingLog(13))
            .thenReturn(EatingLog(startTime = LogDate(firstMealDateTime)))
        val intent = Intent().apply {
            putExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, 13)
        }
        editEatingLogViewModel.onActivityCreated(intent)

        editEatingLogViewModel.onEditFirstMeal()

        val expectedIntentArguments = mapOf(
            DateDialogFragment.DATE_INIT_YEAR to 2019,
            DateDialogFragment.DATE_INIT_MONTH to 3,
            DateDialogFragment.DATE_INIT_DAY to 13)

        editEatingLogViewModel.showDialogFragment.observeForever {
            val intentArguments = assertNotNull(it.getContentIfNotHandled()?.arguments)
            expectedIntentArguments.forEach { (key, value) ->
                assertThat(intentArguments.getInt(key), equalTo(value))
            }
        }
    }

    @Test
    fun onDateSaved_calledWithoutSpecifyingEditMode_throwError() {
        assertFailsWith(IllegalStateException::class) {
            editEatingLogViewModel.onDateSaved(10, 5, 2019)
        }
    }

    @Test
    fun onDateSaved_alreadyInEditMode_throwError() {
        editEatingLogViewModel.onEditFirstMeal()
        editEatingLogViewModel.onDateSaved(10, 5, 2019)

        assertFailsWith(IllegalStateException::class) {
            editEatingLogViewModel.onDateSaved(10, 5, 2019)
        }
    }

    @Test
    fun onTimeSaved_calledBeforeEditingDate_throwError() {
        assertFailsWith(IllegalStateException::class) {
            editEatingLogViewModel.onTimeSaved(10, 5)
        }
    }

    @Test
    fun onDateSaved_showTimeFragment() {
        editEatingLogViewModel.onEditFirstMeal()

        editEatingLogViewModel.onDateSaved(10, 5, 2019)

        editEatingLogViewModel.showDialogFragment.observeForever {
            assertThat(it.getContentIfNotHandled(), IsInstanceOf(TimeDialogFragment::class.java))
        }
    }

    @Test
    fun onTimeSaved_updateCurrentEatingLog() {
        editEatingLogViewModel.onEditFirstMeal()
        editEatingLogViewModel.onDateSaved(10, 5, 2019)

        editEatingLogViewModel.onTimeSaved(10, 25)

        editEatingLogViewModel.firstMealLogTimeObservable.observeForever {
            assertThat(it, containsString("10/06/2019 10:25"))
        }
    }

    @Test
    fun onTimeEditCancelled_resetsEditMode() {
        editEatingLogViewModel.onEditFirstMeal()
        editEatingLogViewModel.onDateSaved(10, 5, 2019)

        editEatingLogViewModel.onTimeEditCancelled()

        assertFailsWith(IllegalStateException::class) {
            editEatingLogViewModel.onTimeSaved(10, 5)
        }
    }

    @Test
    fun onDateEditCancelled_resetsEditMode() {
        editEatingLogViewModel.onEditFirstMeal()

        editEatingLogViewModel.onDateEditCancelled()

        assertFailsWith(IllegalStateException::class) {
            editEatingLogViewModel.onDateSaved(10, 5, 2019)
        }
    }

    @Test
    fun onDiscardButtonClicked_noop() = testScope.runBlockingTest {
        val firstMealOriginalDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 10, 20)
        val lastMealOriginalDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 15, 20)
        whenever(getEatingLog(13)).
            thenReturn(EatingLog(startTime = LogDate(firstMealOriginalDateTime), endTime = LogDate(lastMealOriginalDateTime)))
        val intent = Intent().apply {
            putExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, 13)
        }
        editEatingLogViewModel.onActivityCreated(intent)
        editEatingLogViewModel.onEditLastMeal()
        editEatingLogViewModel.onDateSaved(13, 3, 2019)
        editEatingLogViewModel.onTimeSaved(18, 21)

        editEatingLogViewModel.onEditFirstMeal()
        editEatingLogViewModel.onDateSaved(13, 3, 2019)
        editEatingLogViewModel.onTimeSaved(11, 17)

        editEatingLogViewModel.onDiscardButtonClicked()

        verifyZeroInteractions(updateEatingLog)
    }

    @Test
    fun onSaveButtonClicked_updatesEatingLog() = testScope.runBlockingTest {
        val firstMealOriginalDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 10, 20)
        val lastMealOriginalDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 15, 20)
        whenever(getEatingLog(13))
            .thenReturn(
                EatingLog(startTime = LogDate(firstMealOriginalDateTime),
                    endTime = LogDate(lastMealOriginalDateTime)))
        val intent = Intent().apply {
            putExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, 13)
        }
        editEatingLogViewModel.onActivityCreated(intent)
        editEatingLogViewModel.onEditLastMeal()
        editEatingLogViewModel.onDateSaved(13, 3, 2019)
        editEatingLogViewModel.onTimeSaved(18, 21)

        editEatingLogViewModel.onEditFirstMeal()
        editEatingLogViewModel.onDateSaved(13, 3, 2019)
        editEatingLogViewModel.onTimeSaved(11, 17)

        editEatingLogViewModel.onSaveButtonClicked()

        argumentCaptor<EatingLog>().apply {
            verify(updateEatingLog).invoke(capture())
            firstValue.startTime.apply {
                assertThatMsAreEqualToDateTime(this!!.dateTimeInMillis, 2019, 3, 13, 11, 17)
            }
            firstValue.endTime.apply {
                assertThatMsAreEqualToDateTime(this!!.dateTimeInMillis, 2019, 3, 13, 18, 21)
            }
        }
    }

    @Test
    fun onSaveButtonClicked_firstMealNotEdited_saveFirstMealOriginalDateTime() = testScope.runBlockingTest {
        val firstMealOriginalDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 10, 20)
        val lastMealOriginalDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 15, 20)
        whenever(getEatingLog(13))
            .thenReturn(
                EatingLog(
                    startTime = LogDate(firstMealOriginalDateTime),
                    endTime = LogDate(lastMealOriginalDateTime)))
        val intent = Intent().apply {
            putExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, 13)
        }
        editEatingLogViewModel.onActivityCreated(intent)
        editEatingLogViewModel.onEditLastMeal()
        editEatingLogViewModel.onDateSaved(13, 3, 2019)
        editEatingLogViewModel.onTimeSaved(18, 21)

        editEatingLogViewModel.onSaveButtonClicked()

        argumentCaptor<EatingLog>().apply {
            verify(updateEatingLog).invoke(capture())
            firstValue.startTime.apply {
                assertThatMsAreEqualToDateTime(this!!.dateTimeInMillis, 2019, 3, 13, 10, 20)
            }
            firstValue.endTime.apply {
                assertThatMsAreEqualToDateTime(this!!.dateTimeInMillis, 2019, 3, 13, 18, 21)
            }
        }
    }

    @Test
    fun onSaveButtonClicked_lastMealNotEdited_saveLastMealOriginalDateTime() = testScope.runBlockingTest {
        val firstMealOriginalDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 10, 20)
        val lastMealOriginalDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 15, 20)
        whenever(getEatingLog(13))
            .thenReturn(
                EatingLog(
                    startTime = LogDate(firstMealOriginalDateTime),
                    endTime = LogDate(lastMealOriginalDateTime)))
        val intent = Intent().apply {
            putExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, 13)
        }
        editEatingLogViewModel.onActivityCreated(intent)
        editEatingLogViewModel.onEditFirstMeal()
        editEatingLogViewModel.onDateSaved(13, 3, 2019)
        editEatingLogViewModel.onTimeSaved(13, 54)

        editEatingLogViewModel.onSaveButtonClicked()

        argumentCaptor<EatingLog>().apply {
            verify(updateEatingLog).invoke(capture())
            firstValue.startTime.apply {
                assertThatMsAreEqualToDateTime(this!!.dateTimeInMillis, 2019, 3, 13, 13, 54)
            }
            firstValue.endTime.apply {
                assertThatMsAreEqualToDateTime(this!!.dateTimeInMillis, 2019, 3, 13, 15, 20)
            }
        }
    }

    private fun assertThatMsAreEqualToDateTime(
        millis: Long, year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        assertThat(DateTimeUtils.getYearFromMillis(millis), equalTo(year))
        assertThat(DateTimeUtils.getMonthFromMillis(millis), equalTo(month))
        assertThat(DateTimeUtils.getDayOfMonthFromMillis(millis), equalTo(day))
        assertThat(DateTimeUtils.getHourFromMillis(millis), equalTo(hour))
        assertThat(DateTimeUtils.getMinuteFromMillis(millis), equalTo(minute))
    }

    @Singleton
    @Component(modules = [IFNotesDatabaseTestModule::class])
    interface TestComponent {
        fun inject(editEatingLogViewModelTest: EditEatingLogViewModelTest)

        @Component.Factory
        interface Factory {
            fun create(
                @BindsInstance application: Application,
                @BindsInstance @MainScope mainScope: CoroutineScope,
                @BindsInstance @IODispatcher ioDispatcher: CoroutineDispatcher,
                @BindsInstance updateEatingLog: UpdateEatingLog,
                @BindsInstance getEatingLog: GetEatingLog) : TestComponent
        }
    }
}
