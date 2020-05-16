package com.example.mateusz.ifnotes.ifnotes

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScheduler
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.database.IFNotesDatabaseTestModule
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import com.example.mateusz.ifnotes.domain.usecases.LogMostRecentMeal
import com.example.mateusz.ifnotes.domain.usecases.ObserveMostRecentEatingLog
import com.example.mateusz.ifnotes.lib.SystemClockWrapper
import com.example.mateusz.ifnotes.livedata.testObserve
import com.example.mateusz.ifnotes.presentation.ifnotes.IFNotesViewModel
import com.example.mateusz.ifnotes.presentation.ifnotes.IFNotesViewModel.Companion.DARK_GREEN
import com.example.mateusz.ifnotes.presentation.ifnotes.IFNotesViewModel.Companion.DARK_RED
import com.example.mateusz.ifnotes.presentation.ifnotes.IFNotesViewModel.TimeSinceLastActivityChronometerData
import com.google.common.base.Optional
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.BindsInstance
import dagger.Component
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
class IFNotesViewModelTest {
    @Mock
    private lateinit var clock: Clock
    @Mock
    private lateinit var systemClock: SystemClockWrapper
    @Mock
    private lateinit var logMostRecentMeal: LogMostRecentMeal
    @Mock
    private lateinit var observeMostRecentEatingLog: ObserveMostRecentEatingLog
    private val testScope = TestCoroutineScope()
    private val testScheduler = Schedulers.trampoline()
    private val testDispatcher = TestCoroutineDispatcher()

    private val finishedEatingLog =
        EatingLog(
            startTime = LogDate(2000L, ""),
            endTime = LogDate(4000L, ""))

    private val unfinishedEatingLog =
        EatingLog(
            startTime = LogDate(2000L, ""))

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Inject
    lateinit var ifNotesViewModel: IFNotesViewModel

    private lateinit var component: TestComponent

    @Before
    fun setUp() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        component =
            DaggerIFNotesViewModelTest_TestComponent
                .factory()
                .create(application,
                    clock,
                    systemClock,
                    testScope,
                    testScheduler,
                    testDispatcher,
                    logMostRecentMeal,
                    observeMostRecentEatingLog)
        createIfNotesViewModel()
    }

    @After
    fun cleanUp() {
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun timeSinceLastActivity_mostRecentEatingLogIsNull() {
        whenever(observeMostRecentEatingLog()).thenReturn(Flowable.fromArray(Optional.absent()))
        assertThat(ifNotesViewModel.timeSinceLastActivity.testObserve(), `is`(nullValue()))
    }

    @Test
    fun onLogButtonClicked_logsMostRecentMeal() = testScope.runBlockingTest {
        whenever(clock.millis()).thenReturn(2000L)
        ifNotesViewModel.onLogButtonClicked()

        verify(logMostRecentMeal).invoke(LogDate(2000L, ""))
    }

    @Test
    fun onLogButtonClicked_calledMultipleTimes_logEachTime() = testScope.runBlockingTest {
        whenever(clock.millis()).thenReturn(2000L)
        ifNotesViewModel.onLogButtonClicked()
        verify(logMostRecentMeal).invoke(LogDate(2000L, ""))

        whenever(clock.millis()).thenReturn(5000L)
        ifNotesViewModel.onLogButtonClicked()
        verify(logMostRecentMeal).invoke(LogDate(5000L, ""))
    }

    @Test
    fun timeSinceLastActivity_currentEatinLogFinised() {
        whenever(observeMostRecentEatingLog()).thenReturn(Flowable.fromArray(Optional.of(finishedEatingLog)))
        setCurrentTime(6000L)

        assertThat(ifNotesViewModel.timeSinceLastActivity.testObserve(),
            `is`(equalTo(TimeSinceLastActivityChronometerData(getAdjustedTimeSinceLastActivity(2000L), DARK_GREEN))))
    }

    @Test
    fun timeSinceLastActivity_currentEatinLogUnfinised() {
        whenever(observeMostRecentEatingLog()).thenReturn(Flowable.fromArray(Optional.of(unfinishedEatingLog)))
        setCurrentTime(6000L)

        assertThat(ifNotesViewModel.timeSinceLastActivity.testObserve(),
            `is`(equalTo(TimeSinceLastActivityChronometerData(getAdjustedTimeSinceLastActivity(4000L), DARK_RED))))
    }

    @Test
    fun logButtonState_currentEatingLogUnfinished_isLastMeal() {
        whenever(observeMostRecentEatingLog()).thenReturn(Flowable.fromArray(Optional.of(unfinishedEatingLog)))

        assertThat(ifNotesViewModel.logButtonState.testObserve(), `is`(equalTo(IFNotesViewModel.LogState.LAST_MEAL)))
    }

    @Test
    fun logButtonState_currentEatingLogFinished_isFirstMeal() {
        whenever(observeMostRecentEatingLog()).thenReturn(Flowable.fromArray(Optional.of(finishedEatingLog)))

        assertThat(ifNotesViewModel.logButtonState.testObserve(), `is`(equalTo(IFNotesViewModel.LogState.FIRST_MEAL)))
    }

    private fun createIfNotesViewModel() {
        component.inject(this@IFNotesViewModelTest)
    }

    private fun getAdjustedTimeSinceLastActivity(baseTime: Long): Long {
        return systemClock.elapsedRealtime() - baseTime
    }

    private fun setCurrentTime(time: Long) {
        whenever(clock.millis()).thenReturn(time)
        whenever(systemClock.elapsedRealtime()).thenReturn(time)
    }

    @Singleton
    @Component(modules = [IFNotesDatabaseTestModule::class])
    interface TestComponent {
        fun inject(ifNotesViewModelTest: IFNotesViewModelTest)

        @Component.Factory
        interface Factory {
            fun create(@BindsInstance application: Application,
                       @BindsInstance clock: Clock,
                       @BindsInstance systemClock: SystemClockWrapper,
                       @BindsInstance @MainScope testScope: CoroutineScope,
                       @BindsInstance @MainScheduler testScheduler: Scheduler,
                       @BindsInstance @IODispatcher ioDispatcher: CoroutineDispatcher,
                       @BindsInstance logMostRecentMeal: LogMostRecentMeal,
                       @BindsInstance observeMostRecentEatingLog: ObserveMostRecentEatingLog)
                : TestComponent
        }
    }
}
