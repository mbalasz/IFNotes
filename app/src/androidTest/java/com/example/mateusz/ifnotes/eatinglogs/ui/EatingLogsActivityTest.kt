package com.example.mateusz.ifnotes.eatinglogs.ui

import android.content.Intent
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.R
import com.example.mateusz.ifnotes.component.DaggerTestComponent
import com.example.mateusz.ifnotes.component.IFNotesApplication
import com.example.mateusz.ifnotes.component.InjectionActivityTestRule
import com.example.mateusz.ifnotes.component.TestComponent
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.matchers.RecyclerViewMatcher
import com.example.mateusz.ifnotes.model.data.LogDate
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.RuntimeException
import java.time.Clock

@RunWith(AndroidJUnit4::class)
class EatingLogsActivityTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var repository: Repository
    var clock = mock<Clock>()

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
    private val testScheduler = Schedulers.trampoline()

    @get:Rule
    var injectionActivityTestRule = InjectionActivityTestRule(
        EatingLogsActivity::class.java,
        DaggerTestComponent.factory().create(
            ApplicationProvider.getApplicationContext(),
            testScope,
            testDispatcher,
            testScheduler,
            clock),
        false)

    @After
    fun cleanUp() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun init_noLogs_showsEmptyList() {
        injectionActivityTestRule.launchActivity(Intent())
        onView(withId(R.id.eatingLogsRecyclerView)).check(RecyclerViewCountAssertion(0))
    }

    @Test
    fun displayLogsList() {
        val activity = injectionActivityTestRule.launchActivity(Intent())
        val component = ApplicationProvider.getApplicationContext<IFNotesApplication>().component as TestComponent
        repository = component.repository()
        activity.runOnUiThread {
            testDispatcher.runBlockingTest {
                repository.insertEatingLog(EatingLog(
                    startTime = LogDate(DateTimeUtils.dateTimeToMillis(1, 2, 2019, 10, 50)),
                    endTime = LogDate(DateTimeUtils.dateTimeToMillis(1, 2, 2019, 18, 50))))
                repository.insertEatingLog(EatingLog(
                    startTime = LogDate(DateTimeUtils.dateTimeToMillis(2, 2, 2019, 10, 50)),
                    endTime = LogDate(DateTimeUtils.dateTimeToMillis(2, 2, 2019, 18, 50))))
                repository.insertEatingLog(EatingLog(
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
        val activity = injectionActivityTestRule.launchActivity(Intent())
        val component = ApplicationProvider.getApplicationContext<IFNotesApplication>().component as TestComponent
        repository = component.repository()
        activity.runOnUiThread {
            testDispatcher.runBlockingTest {
                repository.insertEatingLog(EatingLog(
                    startTime = LogDate(DateTimeUtils.dateTimeToMillis(1, 2, 2019, 10, 50)),
                    endTime = LogDate(DateTimeUtils.dateTimeToMillis(1, 2, 2019, 18, 50))))
                repository.insertEatingLog(EatingLog(
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
}
