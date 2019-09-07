package com.example.mateusz.ifnotes.eatinglogs.editlog.ui

import android.content.Intent
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.R
import com.example.mateusz.ifnotes.component.DaggerTestComponent
import com.example.mateusz.ifnotes.component.IFNotesApplication
import com.example.mateusz.ifnotes.component.InjectionActivityTestRule
import com.example.mateusz.ifnotes.component.TestComponent
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogViewModel
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.util.DateTimeTestUtils.Companion.assertThatMsAreEqualToDateTime
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class EditEatingLogActivityTest {

    companion object {
        private val DEFAULT_START_TIME = DateTimeUtils.dateTimeToMillis(5, 0, 2019, 10, 50)
        private val DEFAULT_END_TIME = DateTimeUtils.dateTimeToMillis(5, 0, 2019, 19, 50)
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var repository: Repository
    private lateinit var application: IFNotesApplication
    private lateinit var intent: Intent

    private val testScope = TestCoroutineScope()

    @get:Rule
    var injectionActivityTestRule = InjectionActivityTestRule(
        EditEatingLogActivity::class.java,
        DaggerTestComponent.factory().create(
            ApplicationProvider.getApplicationContext(),
            testScope,
            TestCoroutineDispatcher(),
            TestScheduler(),
            Clock.system(ZoneId.systemDefault())))

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        val component = application.component as TestComponent
        repository = component.repository()
    }

    @After
    fun cleanUp() {
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun editFirstMeal() = testScope.runBlockingTest {
        val originalEatingLog =
            EatingLog(id = 3, startTime = DEFAULT_START_TIME, endTime = DEFAULT_END_TIME)
        repository.insertEatingLog(originalEatingLog)
        startActivity(3)

        setDateTime(R.id.editFirstMealButton, 2019, 1, 4, 18, 50)
        onView(withId(R.id.saveButton)).perform(click())

        val eatingLogs = repository.getEatingLogsObservable().test().awaitCount(1).values()[0]
        assertThat(eatingLogs.size, `is`(1))
        assertThatMsAreEqualToDateTime(eatingLogs[0].startTime, 4, 0, 2019, 18, 50)
    }

    @Test
    fun editLastMeal() = testScope.runBlockingTest {
        val originalEatingLog =
            EatingLog(id = 3, startTime = DEFAULT_START_TIME, endTime = DEFAULT_END_TIME)
        repository.insertEatingLog(originalEatingLog)
        startActivity(3)

        setDateTime(R.id.editLastMealButton, 2019, 1, 5, 21, 31)
        onView(withId(R.id.saveButton)).perform(click())

        val eatingLogs = repository.getEatingLogsObservable().test().awaitCount(1).values()[0]
        assertThat(eatingLogs.size, `is`(1))
        assertThatMsAreEqualToDateTime(eatingLogs[0].endTime, 5, 0, 2019, 21, 31)
    }

    @Test
    fun discard_doesNotSaveChanges() = testScope.runBlockingTest {
        val originalEatingLog =
            EatingLog(id = 3, startTime = DEFAULT_START_TIME, endTime = DEFAULT_END_TIME)
        repository.insertEatingLog(originalEatingLog)
        startActivity(3)

        setDateTime(R.id.editFirstMealButton, 2019, 1, 4, 18, 50)
        setDateTime(R.id.editLastMealButton, 2019, 1, 5, 21, 31)
        onView(withId(R.id.discardButton)).perform(click())

        val eatingLogs = repository.getEatingLogsObservable().test().awaitCount(1).values()[0]
        assertThat(eatingLogs.size, `is`(1))
        assertThatMsAreEqualToDateTime(eatingLogs[0].startTime, 5, 0, 2019, 10, 50)
        assertThatMsAreEqualToDateTime(eatingLogs[0].endTime, 5, 0, 2019, 19, 50)
    }

    private fun startActivity(eatingLogId: Int): ActivityScenario<EditEatingLogActivity> {
        intent = Intent(application, EditEatingLogActivity::class.java).apply {
            putExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, eatingLogId)
        }
        return ActivityScenario.launch(intent)
    }

    private fun setDateTime(viewId: Int, year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        onView(withId(viewId)).perform(click())
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(year, month, day))
        onView(withText("OK")).perform(click())

        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(hour, minute))
        onView(withText("SAVE")).perform(click())
    }
}
