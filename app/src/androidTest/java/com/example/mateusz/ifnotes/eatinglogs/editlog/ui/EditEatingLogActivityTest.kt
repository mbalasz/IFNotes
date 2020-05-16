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
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.R
import com.example.mateusz.ifnotes.component.DaggerTestComponent
import com.example.mateusz.ifnotes.component.IFNotesApplication
import com.example.mateusz.ifnotes.component.InjectionActivityTestRule
import com.example.mateusz.ifnotes.component.TestComponent
import com.example.mateusz.ifnotes.date.DateTimeTestUtils.Companion.assertThatMsAreEqualToDateTime
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import com.example.mateusz.ifnotes.domain.usecases.InsertEatingLog
import com.example.mateusz.ifnotes.domain.usecases.ObserveEatingLogs
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogViewModel
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
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

    private lateinit var application: IFNotesApplication
    private lateinit var intent: Intent
    private lateinit var observeEatingLogs: ObserveEatingLogs
    private lateinit var insertEatingLog: InsertEatingLog

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
        observeEatingLogs = component.observeEatingLogs()
        insertEatingLog = component.insertEatignLog()
    }

    @After
    fun cleanUp() {
        testScope.cleanupTestCoroutines()
    }

    // We have to use runBlocking because this test executes a room transaction. 
    // Here's a good explanation why we need to use runBlocking instead of runBlockingTest
    // https://medium.com/@eyalg/testing-androidx-room-kotlin-coroutines-2d1faa3e674f.
    // Once https://github.com/Kotlin/kotlinx.coroutines/pull/1206 is fixed try replacing
    // runBlocking with runBlockingTest.
    // Same applies to other tests in this class
    @Test
    fun editFirstMeal() = runBlocking<Unit> {
        val originalEatingLog =
            EatingLog(id = 3, startTime = LogDate(DEFAULT_START_TIME), endTime = LogDate(DEFAULT_END_TIME))
        insertEatingLog(originalEatingLog)
        startActivity(3)

        setDateTime(R.id.editFirstMealButton, 2019, 1, 4, 18, 50)
        onView(withId(R.id.saveButton)).perform(click())

        val eatingLogs = observeEatingLogs().test().awaitCount(1).values()[0]
        assertThat(eatingLogs.size, `is`(1))
        assertThatMsAreEqualToDateTime(eatingLogs[0].startTime!!.dateTimeInMillis, 4, 0, 2019, 18, 50)
    }

    @Test
    fun editLastMeal() = runBlocking<Unit> {
        val originalEatingLog =
            EatingLog(id = 3, startTime = LogDate(DEFAULT_START_TIME), endTime = LogDate(DEFAULT_END_TIME))
        insertEatingLog(originalEatingLog)
        startActivity(3)

        setDateTime(R.id.editLastMealButton, 2019, 1, 5, 21, 31)
        onView(withId(R.id.saveButton)).perform(click())

        val eatingLogs = observeEatingLogs().test().awaitCount(1).values()[0]
        assertThat(eatingLogs.size, `is`(1))
        assertThatMsAreEqualToDateTime(eatingLogs[0].endTime!!.dateTimeInMillis, 5, 0, 2019, 21, 31)
    }

    @Test
    fun discard_doesNotSaveChanges() = runBlocking<Unit> {
        val originalEatingLog =
            EatingLog(id = 3, startTime = LogDate(DEFAULT_START_TIME), endTime = LogDate(DEFAULT_END_TIME))
        insertEatingLog(originalEatingLog)
        startActivity(3)

        setDateTime(R.id.editFirstMealButton, 2019, 1, 4, 18, 50)
        setDateTime(R.id.editLastMealButton, 2019, 1, 5, 21, 31)
        onView(withId(R.id.discardButton)).perform(click())

        val eatingLogs = observeEatingLogs().test().awaitCount(1).values()[0]
        assertThat(eatingLogs.size, `is`(1))
        assertThatMsAreEqualToDateTime(eatingLogs[0].startTime!!.dateTimeInMillis, 5, 0, 2019, 10, 50)
        assertThatMsAreEqualToDateTime(eatingLogs[0].endTime!!.dateTimeInMillis, 5, 0, 2019, 19, 50)
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
