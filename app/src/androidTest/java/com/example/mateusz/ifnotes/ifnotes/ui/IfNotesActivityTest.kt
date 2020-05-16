package com.example.mateusz.ifnotes.ifnotes.ui

import android.widget.TimePicker
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.R
import com.example.mateusz.ifnotes.component.AppModule
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScheduler
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.component.IFNotesApplication
import com.example.mateusz.ifnotes.component.InjectionActivityTestRule
import com.example.mateusz.ifnotes.data.EatingLogsRepositoryImpl
import com.example.mateusz.ifnotes.data.RepositoryModule
import com.example.mateusz.ifnotes.database.IFNotesDatabaseTestModule
import com.example.mateusz.ifnotes.database.IFNotesDatabaseTestModule.QueryExecutor
import com.example.mateusz.ifnotes.date.DateTimeTestUtils.Companion.assertThatMsAreEqualToDateTime
import com.example.mateusz.ifnotes.date.DateTimeTestUtils.Companion.assertThatMsAreEqualToTime
import com.example.mateusz.ifnotes.domain.usecases.InsertEatingLog
import com.example.mateusz.ifnotes.domain.usecases.ObserveEatingLogs
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.presentation.ifnotes.IFNotesModule
import com.example.mateusz.ifnotes.presentation.ifnotes.ui.IFNotesActivity
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.util.concurrent.Executor
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
class IfNotesActivityTest {

    private lateinit var EatingLogsRepositoryImpl: EatingLogsRepositoryImpl
    private lateinit var observeEatingLogs: ObserveEatingLogs
    var clock = mock<Clock>()

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
    private val testScheduler = Schedulers.trampoline()

    @get:Rule
    var injectionActivityTestRule = InjectionActivityTestRule(
        IFNotesActivity::class.java,
        DaggerIfNotesActivityTest_TestComponent.factory().create(
            ApplicationProvider.getApplicationContext(),
            testScope,
            testDispatcher,
            testScheduler,
            clock,
            // We need a main thread executor for Room. Otherwise, calling insertEatingLog within a
            // transaction synchronously leads to calling observeEatingLogs() query on a db and
            // that query is not a suspend DAO method and it shouldn't be.
            ArchTaskExecutor.getMainThreadExecutor()))

    @Before
    fun setUp() {
        val component = ApplicationProvider.getApplicationContext<IFNotesApplication>().component as TestComponent
        observeEatingLogs = component.observeEatingLogs()
    }

    @After
    fun cleanUp() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun logActivity_init() = testDispatcher.runBlockingTest {
        whenever(clock.millis()).thenReturn(DateTimeUtils.timeToMillis(10, 50))

        onView(withId(R.id.logActivityButton)).perform(click())

        observeEatingLogs().test().awaitCount(1).assertOf { subscriber ->
            val eatingLog = subscriber.values()[0].let {
                assertThat(it.size, `is`(1))
                it[0]
            }
            assertThatMsAreEqualToTime(eatingLog.startTime!!.dateTimeInMillis, 10, 50)
            assertThat(eatingLog.endTime, `is`(nullValue()))
        }
    }

    @Test
    fun logActivity_lastMeal() = testDispatcher.runBlockingTest {
        whenever(clock.millis()).thenReturn(DateTimeUtils.timeToMillis(10, 50))
        onView(withId(R.id.logActivityButton)).perform(click())

        whenever(clock.millis()).thenReturn(DateTimeUtils.timeToMillis(18, 50))
        onView(withId(R.id.logActivityButton)).perform(click())

        observeEatingLogs().test().awaitCount(1).assertOf { subscriber ->
            val eatingLog = subscriber.values()[0].let {
                assertThat(it.size, `is`(1))
                it[0]
            }
            assertThatMsAreEqualToTime(eatingLog.startTime!!.dateTimeInMillis, 10, 50)
            assertThatMsAreEqualToTime(eatingLog.endTime!!.dateTimeInMillis, 18, 50)
        }
    }

    @Test
    fun logActivity_newFirstMeal() = testDispatcher.runBlockingTest {
        whenever(clock.millis()).thenReturn(
            DateTimeUtils.dateTimeToMillis(1, 2, 2019,10, 50))
        onView(withId(R.id.logActivityButton)).perform(click())
        whenever(clock.millis()).thenReturn(
            DateTimeUtils.dateTimeToMillis(1, 2, 2019,18, 50))
        onView(withId(R.id.logActivityButton)).perform(click())

        whenever(clock.millis()).thenReturn(
            DateTimeUtils.dateTimeToMillis(2, 2, 2019,9, 30))
        onView(withId(R.id.logActivityButton)).perform(click())

        observeEatingLogs().test().awaitCount(1).assertOf { subscriber ->
            val eatingLogs = subscriber.values()[0].also {
                assertThat(it.size, `is`(2))
            }
            assertThatMsAreEqualToDateTime(
                eatingLogs[0].startTime!!.dateTimeInMillis, 1, 2, 2019, 10, 50)
            assertThatMsAreEqualToDateTime(
                eatingLogs[0].endTime!!.dateTimeInMillis, 1, 2, 2019,  18, 50)
            assertThatMsAreEqualToDateTime(
                eatingLogs[1].startTime!!.dateTimeInMillis, 2, 2, 2019, 9, 30)
            assertThat(eatingLogs[1].endTime, `is`(nullValue()))
        }
    }

    @Test
    fun manualLog() = testDispatcher.runBlockingTest {
        whenever(clock.millis()).thenReturn(DateTimeUtils.timeToMillis(10, 50))

        onView(withId(R.id.manualLogButton)).perform(click())
        onView(withClassName(Matchers.equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(10, 30))
        onView(ViewMatchers.withText("SAVE")).perform(click())

        onView(withId(R.id.manualLogButton)).perform(click())
        onView(withClassName(Matchers.equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(10, 45))
        onView(ViewMatchers.withText("SAVE")).perform(click())

        observeEatingLogs().test().awaitCount(1).assertOf { subscriber ->
            val eatingLogs = subscriber.values()[0].also {
                assertThat(it.size, `is`(1))
            }
            assertThatMsAreEqualToTime(eatingLogs[0].startTime!!.dateTimeInMillis, 10, 30)
            assertThatMsAreEqualToTime(eatingLogs[0].endTime!!.dateTimeInMillis, 10, 45)
        }
    }

    @Test
    fun logShortTimeAgo_firstMeal() = testDispatcher.runBlockingTest {
        whenever(clock.millis()).thenReturn(DateTimeUtils.timeToMillis(10, 50))

        onView(withId(R.id.logShortTimeAgo)).perform(click())

        observeEatingLogs().test().awaitCount(1).assertOf { subscriber ->
            val eatingLog = subscriber.values()[0].let {
                assertThat(it.size, `is`(1))
                it[0]
            }

            assertThatMsAreEqualToTime(eatingLog.startTime!!.dateTimeInMillis, 10, 35)
            assertThat(eatingLog.endTime,`is`(nullValue()))
        }
    }

    @Test
    fun logMidTimeAgo_firstMeal() = testDispatcher.runBlockingTest {
        whenever(clock.millis()).thenReturn(DateTimeUtils.timeToMillis(10, 50))

        onView(withId(R.id.logMidTimeAgo)).perform(click())

        observeEatingLogs().test().awaitCount(1).assertOf { subscriber ->
            val eatingLog = subscriber.values()[0].let {
                assertThat(it.size, `is`(1))
                it[0]
            }

            assertThatMsAreEqualToTime(eatingLog.startTime!!.dateTimeInMillis, 10, 20)
            assertThat(eatingLog.endTime, `is`(nullValue()))
        }
    }

    @Test
    fun logLongTimeAgo_firstMeal() = testDispatcher.runBlockingTest {
        whenever(clock.millis()).thenReturn(DateTimeUtils.timeToMillis(10, 50))

        onView(withId(R.id.logLongTimeAgo)).perform(click())

        observeEatingLogs().test().awaitCount(1).assertOf { subscriber ->
            val eatingLog = subscriber.values()[0].let {
                assertThat(it.size, `is`(1))
                it[0]
            }

            assertThatMsAreEqualToTime(eatingLog.startTime!!.dateTimeInMillis, 9, 50)
            assertThat(eatingLog.endTime, `is`(nullValue()))
        }
    }

    @Test
    fun multipleLogs() = testDispatcher.runBlockingTest {
        whenever(clock.millis()).thenReturn(
            DateTimeUtils.dateTimeToMillis(1, 2, 2019,10, 50))
        onView(withId(R.id.logActivityButton)).perform(click())

        whenever(clock.millis()).thenReturn(
            DateTimeUtils.dateTimeToMillis(1, 2, 2019,17, 50))
        onView(withId(R.id.logMidTimeAgo)).perform(click())

        whenever(clock.millis()).thenReturn(
            DateTimeUtils.dateTimeToMillis(2, 2, 2019,9, 30))
        onView(withId(R.id.logShortTimeAgo)).perform(click())

        whenever(clock.millis()).thenReturn(
            DateTimeUtils.dateTimeToMillis(2, 2, 2019,18, 30))
        onView(withId(R.id.manualLogButton)).perform(click())
        onView(withClassName(Matchers.equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(18, 0))
        onView(ViewMatchers.withText("SAVE")).perform(click())

        whenever(clock.millis()).thenReturn(
            DateTimeUtils.dateTimeToMillis(3, 2, 2019,4, 13))
        onView(withId(R.id.logActivityButton)).perform(click())

        observeEatingLogs().test().awaitCount(1).assertOf { subscriber ->
            val eatingLogs = subscriber.values()[0].also {
                assertThat(it.size, `is`(3))
            }
            assertThatMsAreEqualToDateTime(
                eatingLogs[0].startTime!!.dateTimeInMillis, 1, 2, 2019, 10, 50)
            assertThatMsAreEqualToDateTime(
                eatingLogs[0].endTime!!.dateTimeInMillis, 1, 2, 2019,  17, 20)
            assertThatMsAreEqualToDateTime(
                eatingLogs[1].startTime!!.dateTimeInMillis, 2, 2, 2019, 9, 15)
            assertThatMsAreEqualToDateTime(
                eatingLogs[1].endTime!!.dateTimeInMillis, 2, 2, 2019, 18, 0)
            assertThatMsAreEqualToDateTime(
                eatingLogs[2].startTime!!.dateTimeInMillis, 3, 2, 2019, 4, 13)
            assertThat(eatingLogs[2].endTime, `is`(nullValue()))
        }
    }


    @Singleton
    @Component(modules = [
        AndroidInjectionModule::class,
        IFNotesDatabaseTestModule::class,
        RepositoryModule::class,
        IFNotesModule::class,
        AppModule::class
    ])
    interface TestComponent : AndroidInjector<IFNotesApplication> {
        @Component.Factory
        interface Factory {
            fun create(
                @BindsInstance ifNotesApplication: IFNotesApplication,
                @BindsInstance @MainScope coroutineScope: CoroutineScope,
                @BindsInstance @IODispatcher ioDispatcher: CoroutineDispatcher,
                @BindsInstance @MainScheduler mainScheduler: Scheduler,
                @BindsInstance clock: Clock,
                @BindsInstance @QueryExecutor queryExecutor: Executor): TestComponent
        }

        fun insertEatignLog(): InsertEatingLog

        fun observeEatingLogs(): ObserveEatingLogs
    }
}
