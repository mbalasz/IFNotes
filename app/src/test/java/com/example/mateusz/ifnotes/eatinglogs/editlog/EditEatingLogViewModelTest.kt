package com.example.mateusz.ifnotes.eatinglogs.editlog

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.time.DateDialogFragment
import com.example.mateusz.ifnotes.time.TimeDialogFragment
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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
import java.lang.IllegalStateException
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class EditEatingLogViewModelTest {
    private val testScope = TestCoroutineScope()
    @Mock private lateinit var repository: Repository

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var editEatingLogViewModel: EditEatingLogViewModel

    @Before
    fun setUp() {
        editEatingLogViewModel = EditEatingLogViewModel(
            ApplicationProvider.getApplicationContext(), repository, testScope)
    }

    @After
    fun cleanUp() {
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun init_firstMealLogTimeObservable_emptyString() {
        editEatingLogViewModel.firstMealLogTimeObservable.observeForever {
            assertThat(it, equalTo(""))
        }
    }

    @Test
    fun init_lastMealLogTimeObservable_emptyString() {
        editEatingLogViewModel.lastMealLogTimeObservable.observeForever {
            assertThat(it, equalTo(""))
        }
    }

    @Test
    fun onActivityCreated_firstMealLogDateTimeUpdated() = testScope.runBlockingTest {
        val firstMealDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 10, 20)
        val lastMealDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 18, 50)
        whenever(repository.getEatingLog(13)).
            thenReturn(EatingLog(startTime = firstMealDateTime, endTime = lastMealDateTime))
        val intent = Intent().apply {
            putExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, 13)
        }

        editEatingLogViewModel.onActivityCreated(intent)

        editEatingLogViewModel.firstMealLogTimeObservable.observeForever {
            assertThat(it, containsString("13/04/2019 10:20"))
        }
        editEatingLogViewModel.lastMealLogTimeObservable.observeForever {
            assertThat(it, containsString("13/04/2019 18:50"))
        }
    }

    @Test
    fun onEditFirstMeal_sendsDateDialogFragment() {
        editEatingLogViewModel.onEditFirstMeal()

        editEatingLogViewModel.showDialogFragment.observeForever {
            assertThat(it.getContentIfNotHandled(), IsInstanceOf(DateDialogFragment::class.java))
        }
    }

    @Test
    fun onEditFirstMeal_sendsDateDialogFragmentWithOriginalLogTime() = testScope.runBlockingTest {
        val firstMealDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 10, 20)
        whenever(repository.getEatingLog(13)).
            thenReturn(EatingLog(startTime = firstMealDateTime))
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
    fun onSaveButtonClicked_commitsUpdatesToRepository() = testScope.runBlockingTest {
        val firstMealOriginalDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 10, 20)
        val lastMealOriginalDateTime =
            DateTimeUtils.dateTimeToMillis(13, 3, 2019, 15, 20)
        whenever(repository.getEatingLog(13)).
            thenReturn(EatingLog(startTime = firstMealOriginalDateTime, endTime = lastMealOriginalDateTime))
        val intent = Intent().apply {
            putExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, 13)
        }
        editEatingLogViewModel.onActivityCreated(intent)
        editEatingLogViewModel.onEditLastMeal()
        editEatingLogViewModel.onDateSaved(13, 3, 2019)
        editEatingLogViewModel.onTimeSaved(18, 21)

        editEatingLogViewModel.onSaveButtonClicked()

        argumentCaptor<EatingLog>().apply {
            verify(repository).updateEatingLog(capture())
            firstValue.startTime.apply {
                assertThat(DateTimeUtils.getDayOfMonthFromMillis(this), equalTo(13))
                assertThat(DateTimeUtils.getMonthFromMillis(this), equalTo(3))
                assertThat(DateTimeUtils.getYearFromMillis(this), equalTo(2019))
                assertThat(DateTimeUtils.getHourFromMillis(this), equalTo(10))
                assertThat(DateTimeUtils.getMinuteFromMillis(this), equalTo(20))
            }
            firstValue.endTime.apply {
                assertThat(DateTimeUtils.getDayOfMonthFromMillis(this), equalTo(13))
                assertThat(DateTimeUtils.getMonthFromMillis(this), equalTo(3))
                assertThat(DateTimeUtils.getYearFromMillis(this), equalTo(2019))
                assertThat(DateTimeUtils.getHourFromMillis(this), equalTo(18))
                assertThat(DateTimeUtils.getMinuteFromMillis(this), equalTo(21))
            }
        }
    }
}
