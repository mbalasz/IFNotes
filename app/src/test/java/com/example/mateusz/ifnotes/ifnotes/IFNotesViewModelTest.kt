package com.example.mateusz.ifnotes.ifnotes

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScheduler
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.database.IFNotesDatabaseTestModule
import com.example.mateusz.ifnotes.ifnotes.IFNotesViewModel.TimeSinceLastActivityChronometerData
import com.example.mateusz.ifnotes.lib.SystemClockWrapper
import com.example.mateusz.ifnotes.data.EatingLogsRepositoryImpl
import com.example.mateusz.ifnotes.data.room.EatingLogData
import com.example.mateusz.ifnotes.data.room.LogDateData
import com.google.common.base.Optional
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.BindsInstance
import dagger.Component
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
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
    @Mock private lateinit var EatingLogsRepositoryImpl: EatingLogsRepositoryImpl
    @Mock private lateinit var clock: Clock
    @Mock private lateinit var systemClock: SystemClockWrapper
    private val testScope = TestCoroutineScope()
    private val testScheduler = Schedulers.trampoline()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Inject lateinit var ifNotesViewModel: IFNotesViewModel
    private lateinit var component: TestComponent

    @Before
    fun setUp() {
        whenever(EatingLogsRepositoryImpl.observeMostRecentEatingLog())
            .thenReturn(Flowable.fromArray(Optional.absent()))
        val application = ApplicationProvider.getApplicationContext<Application>()
        component =
            DaggerIFNotesViewModelTest_TestComponent
                .factory()
                .create(application, EatingLogsRepositoryImpl, clock, systemClock, testScope, testScheduler)
    }

    @After
    fun cleanUp() {
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun timeSinceLastActivity_initValueIsNull() = testScope.runBlockingTest {
        createIfNotesViewModel()
        assertThat(ifNotesViewModel.timeSinceLastActivity.value, `is`(nullValue()))
    }

    @Suppress("DeferredResultUnused")
    @Test
    fun onLogButtonClicked_initEatingLogStillBeingLoaded_noop() = runBlocking<Unit> {
        val initEatingLogPublisher = PublishSubject.create<Optional<EatingLogData>>()
        whenever(EatingLogsRepositoryImpl.observeMostRecentEatingLog())
            .thenReturn(initEatingLogPublisher.toFlowable(BackpressureStrategy.BUFFER))
        whenever(clock.millis()).thenReturn(1200L)

        createIfNotesViewModel()

        testScope.runBlockingTest {
            ifNotesViewModel.onLogButtonClicked()
            verify(EatingLogsRepositoryImpl, never()).insertEatingLog(any())
            verify(EatingLogsRepositoryImpl, never()).updateEatingLog(any())

            initEatingLogPublisher.onNext(Optional.of(EatingLogData(startTime = LogDateData(100L))))
        }

        verify(EatingLogsRepositoryImpl).updateEatingLog(any())
    }

    @Test
    fun onLogButtonClicked_noEatingLogInProgress_createsNewEatingLog() = runBlocking<Unit> {
        createIfNotesViewModel()
        whenever(clock.millis()).thenReturn(1200L)

        testScope.runBlockingTest {
            ifNotesViewModel.onLogButtonClicked()
        }

        argumentCaptor<EatingLogData>().apply {
            verify(EatingLogsRepositoryImpl).insertEatingLog(capture())
            assertThat(firstValue.startTime!!.dateTimeInMillis, `is`(equalTo(1200L)))
        }
    }

    @Test
    fun onLogButtonClicked_eatingLogInProgress_updatesCurrentEatingLog() = runBlocking<Unit> {
        val eatingLogInProgress = EatingLogData(id = 1, startTime = LogDateData(300L))
        whenever(EatingLogsRepositoryImpl.observeMostRecentEatingLog())
            .thenReturn(Flowable.fromArray(Optional.of(eatingLogInProgress)))
        createIfNotesViewModel()
        whenever(clock.millis()).thenReturn(1200L)

        testScope.runBlockingTest {
            ifNotesViewModel.onLogButtonClicked()
        }

        argumentCaptor<EatingLogData>().apply {
            verify(EatingLogsRepositoryImpl).updateEatingLog(capture())
            assertThat(firstValue.endTime!!.dateTimeInMillis, `is`(equalTo(1200L)))
        }
    }

    @Test
    fun mostRecentLogUpdated_timeSinceLastActivityIsUpdated() = testScope.runBlockingTest {
        val initEatingLogPublisher = PublishSubject.create<Optional<EatingLogData>>()
        whenever(EatingLogsRepositoryImpl.observeMostRecentEatingLog())
            .thenReturn(initEatingLogPublisher.toFlowable(BackpressureStrategy.BUFFER))
        createIfNotesViewModel()
        whenever(clock.millis()).thenReturn(200L)
        whenever(systemClock.elapsedRealtime()).thenReturn(500L)

        initEatingLogPublisher.onNext(Optional.of(EatingLogData(startTime = LogDateData(100L))))

        var expectedTimeSinceLastActivityChronometerData: TimeSinceLastActivityChronometerData? = null
        ifNotesViewModel.timeSinceLastActivity.observeForever {
            expectedTimeSinceLastActivityChronometerData = it
        }

        assertThat(
            expectedTimeSinceLastActivityChronometerData,
            `is`(equalTo(TimeSinceLastActivityChronometerData(400L, IFNotesViewModel.DARK_RED))))
    }

    private fun createIfNotesViewModel() {
        component.inject(this@IFNotesViewModelTest)
    }

    @Singleton
    @Component(modules = [IFNotesDatabaseTestModule::class])
    interface TestComponent {
        fun inject(ifNotesViewModelTest: IFNotesViewModelTest)

        @Component.Factory
        interface Factory {
            fun create(@BindsInstance application: Application,
                       @BindsInstance EatingLogsRepositoryImpl: EatingLogsRepositoryImpl,
                       @BindsInstance clock: Clock,
                       @BindsInstance systemClock: SystemClockWrapper,
                       @BindsInstance @MainScope testScope: CoroutineScope,
                       @BindsInstance @MainScheduler testScheduler: Scheduler)
                : TestComponent
        }
    }
}
