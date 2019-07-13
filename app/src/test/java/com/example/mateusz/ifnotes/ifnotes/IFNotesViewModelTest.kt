package com.example.mateusz.ifnotes.ifnotes

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.ifnotes.IFNotesViewModel.TimeSinceLastActivityChronometerData
import com.example.mateusz.ifnotes.lib.SystemClockWrapper
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.google.common.base.Optional
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
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

@RunWith(AndroidJUnit4::class)
class IFNotesViewModelTest {
    @Mock private lateinit var repository: Repository
    @Mock private lateinit var clock: Clock
    @Mock private lateinit var systemClock: SystemClockWrapper
    private val testScope = TestCoroutineScope()
    private val testScheduler = TestScheduler()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private lateinit var ifNotesViewModel: IFNotesViewModel

    @Before
    fun setUp() {
        whenever(repository.getMostRecentEatingLog())
            .thenReturn(Flowable.fromArray(Optional.absent()))
        ifNotesViewModel = createIfNotesViewModel()
        testScheduler.triggerActions()
    }

    @After
    fun cleanUp() {
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun timeSinceLastActivity_initValueIsNull() = testScope.runBlockingTest {
        assertThat(ifNotesViewModel.timeSinceLastActivity.value, `is`(nullValue()))
    }

    @Suppress("DeferredResultUnused")
    @Test
    fun onLogButtonClicked_initEatingLogStillBeingLoaded_noop() = runBlocking<Unit> {
        val initEatingLogPublisher = PublishSubject.create<Optional<EatingLog>>()
        whenever(repository.getMostRecentEatingLog())
            .thenReturn(initEatingLogPublisher.toFlowable(BackpressureStrategy.BUFFER))
        whenever(clock.millis()).thenReturn(1200L)

        ifNotesViewModel = createIfNotesViewModel()

        testScope.runBlockingTest {
            ifNotesViewModel.onLogButtonClicked()
            verify(repository, never()).insertEatingLog(any())
            verify(repository, never()).updateEatingLog(any())

            initEatingLogPublisher.onNext(Optional.of(EatingLog(startTime = 100L)))
            testScheduler.triggerActions()
        }

        verify(repository).updateEatingLog(any())
    }

    @Test
    fun onLogButtonClicked_noEatingLogInProgress_createsNewEatingLog() = runBlocking<Unit> {
        whenever(clock.millis()).thenReturn(1200L)

        testScope.runBlockingTest {
            ifNotesViewModel.onLogButtonClicked()
        }

        argumentCaptor<EatingLog>().apply {
            verify(repository).insertEatingLog(capture())
            assertThat(firstValue.startTime, `is`(equalTo(1200L)))
        }
    }

    @Test
    fun onLogButtonClicked_eatingLogInProgress_updatesCurrentEatingLog() = runBlocking<Unit> {
        val eatingLogInProgress = EatingLog(id = 1, startTime = 300L)
        whenever(repository.getMostRecentEatingLog())
            .thenReturn(Flowable.fromArray(Optional.of(eatingLogInProgress)))
        ifNotesViewModel = createIfNotesViewModel()
        testScheduler.triggerActions()
        whenever(clock.millis()).thenReturn(1200L)

        testScope.runBlockingTest {
            ifNotesViewModel.onLogButtonClicked()
        }

        argumentCaptor<EatingLog>().apply {
            verify(repository).updateEatingLog(capture())
            assertThat(firstValue.endTime, `is`(equalTo(1200L)))
        }
    }

    @Test
    fun mostRecentLogUpdated_timeSinceLastActivityIsUpdated() = testScope.runBlockingTest {
        val initEatingLogPublisher = PublishSubject.create<Optional<EatingLog>>()
        whenever(repository.getMostRecentEatingLog())
            .thenReturn(initEatingLogPublisher.toFlowable(BackpressureStrategy.BUFFER))
        ifNotesViewModel = createIfNotesViewModel()
        whenever(clock.millis()).thenReturn(200L)
        whenever(systemClock.elapsedRealtime()).thenReturn(500L)

        initEatingLogPublisher.onNext(Optional.of(EatingLog(startTime = 100L)))
        testScheduler.triggerActions()

        var expectedTimeSinceLastActivityChronometerData: TimeSinceLastActivityChronometerData? = null
        ifNotesViewModel.timeSinceLastActivity.observeForever {
            expectedTimeSinceLastActivityChronometerData = it
        }

        assertThat(
            expectedTimeSinceLastActivityChronometerData,
            `is`(equalTo(TimeSinceLastActivityChronometerData(400L, IFNotesViewModel.DARK_RED))))
    }

    private fun createIfNotesViewModel(): IFNotesViewModel {
        return IFNotesViewModel(
            ApplicationProvider.getApplicationContext(),
            repository,
            clock,
            systemClock,
            testScope,
            testScheduler)
    }
}
