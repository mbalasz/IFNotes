package com.example.mateusz.ifnotes.eatinglogs.ui

import android.view.View
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.R
import com.example.mateusz.ifnotes.component.AppModule
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScheduler
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.component.IFNotesApplication
import com.example.mateusz.ifnotes.component.InjectionActivityTestRule
import com.example.mateusz.ifnotes.data.RepositoryModule
import com.example.mateusz.ifnotes.database.IFNotesDatabaseTestModule
import com.example.mateusz.ifnotes.database.IFNotesDatabaseTestModule.QueryExecutor
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import com.example.mateusz.ifnotes.domain.usecases.InsertEatingLog
import com.example.mateusz.ifnotes.domain.usecases.ObserveEatingLogs
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsModule
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.matchers.RecyclerViewMatcher
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.util.concurrent.Executor
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
class EatingLogsActivityTest {
    var clock = mock<Clock>()

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
    private val testScheduler = Schedulers.trampoline()
    private lateinit var insertEatingLog: InsertEatingLog

    @get:Rule
    var injectionActivityTestRule = InjectionActivityTestRule(
        EatingLogsActivity::class.java,
        DaggerEatingLogsActivityTest_TestComponent.factory().create(
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
        val component = ApplicationProvider.getApplicationContext<IFNotesApplication>().component
            as TestComponent
        insertEatingLog = component.insertEatignLog()
    }

    @After
    fun cleanUp() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun init_noLogs_showsEmptyList() {
        onView(withId(R.id.eatingLogsRecyclerView)).check(RecyclerViewCountAssertion(0))
    }

    @Test
    fun displayLogsList() {
        injectionActivityTestRule.activity.runOnUiThread {
            runBlocking {
                whenever(clock.millis()).thenReturn(DateTimeUtils.dateTimeToMillis(5, 2, 2019, 10, 50))
                insertEatingLog(EatingLog(
                    startTime = LogDate(DateTimeUtils.dateTimeToMillis(1, 2, 2019, 10, 50)),
                    endTime = LogDate(DateTimeUtils.dateTimeToMillis(1, 2, 2019, 18, 50))))
                insertEatingLog(EatingLog(
                    startTime = LogDate(DateTimeUtils.dateTimeToMillis(2, 2, 2019, 10, 50)),
                    endTime = LogDate(DateTimeUtils.dateTimeToMillis(2, 2, 2019, 18, 50))))
                insertEatingLog(EatingLog(
                    startTime = LogDate(DateTimeUtils.dateTimeToMillis(3, 2, 2019, 10, 50)),
                    endTime = LogDate(DateTimeUtils.dateTimeToMillis(3, 2, 2019, 18, 50))))
            }
        }

        onView(withId(R.id.eatingLogsRecyclerView)).check(RecyclerViewCountAssertion(3))
        onView(RecyclerViewMatcher(R.id.eatingLogsRecyclerView).atPosition(0))
            .check(matches(hasDescendant(withSubstring("03/03/2019 10:50"))))
        onView(RecyclerViewMatcher(R.id.eatingLogsRecyclerView).atPosition(0))
            .check(matches(hasDescendant(withSubstring("03/03/2019 18:50"))))
        onView(RecyclerViewMatcher(R.id.eatingLogsRecyclerView).atPosition(1))
            .check(matches(hasDescendant(withSubstring("02/03/2019 10:50"))))
        onView(RecyclerViewMatcher(R.id.eatingLogsRecyclerView).atPosition(1))
            .check(matches(hasDescendant(withSubstring("02/03/2019 18:50"))))
        onView(RecyclerViewMatcher(R.id.eatingLogsRecyclerView).atPosition(2))
            .check(matches(hasDescendant(withSubstring("01/03/2019 10:50"))))
        onView(RecyclerViewMatcher(R.id.eatingLogsRecyclerView).atPosition(2))
            .check(matches(hasDescendant(withSubstring("01/03/2019 18:50"))))
    }


    @Test
    fun removeLog() {
        injectionActivityTestRule.activity.runOnUiThread {
            runBlocking {
                whenever(clock.millis()).thenReturn(DateTimeUtils.dateTimeToMillis(5, 2, 2019, 10, 50))
                insertEatingLog(EatingLog(
                    startTime = LogDate(DateTimeUtils.dateTimeToMillis(1, 2, 2019, 10, 50)),
                    endTime = LogDate(DateTimeUtils.dateTimeToMillis(1, 2, 2019, 18, 50))))
                insertEatingLog(EatingLog(
                    startTime = LogDate(DateTimeUtils.dateTimeToMillis(2, 2, 2019, 10, 50)),
                    endTime = LogDate(DateTimeUtils.dateTimeToMillis(2, 2, 2019, 18, 50))))
            }
        }

        onView(withId(R.id.eatingLogsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition<EatingLogsAdapter.EatingLogViewHolder>(
                0,
                ClickChildViewWithId(R.id.item_row_remove_button)))
        onView(withId(R.id.eatingLogsRecyclerView)).check(RecyclerViewCountAssertion(1))
        onView(withId(R.id.eatingLogsRecyclerView))
            .check(matches(not(hasDescendant(withSubstring("02/03/2019 10:50")))))
    }

    // TODO: Add test for the edit button on a log entry.

    class ClickChildViewWithId(private val id: Int) : ViewAction {
        override fun getDescription(): String {
            return "Click on a child view with specified id."
        }

        override fun getConstraints(): Matcher<View>? = null

        override fun perform(uiController: UiController?, view: View?) {
            view?.findViewById<View>(id)?.performClick()
        }
    }

    class RecyclerViewCountAssertion(private val count: Int) : ViewAssertion {
        override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
            if (view !is RecyclerView) {
                throw RuntimeException(noViewFoundException)
            }
            assertThat(view.adapter?.itemCount, equalTo(count))
        }

    }

    @Singleton
    @Component(modules = [
        AndroidInjectionModule::class,
        IFNotesDatabaseTestModule::class,
        RepositoryModule::class,
        EatingLogsModule::class,
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
