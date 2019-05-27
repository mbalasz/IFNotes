package com.example.mateusz.ifnotes.ifnotes

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
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
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import java.time.Clock

@RunWith(AndroidJUnit4::class)
class IFNotesViewModelTest {
    @Mock private lateinit var repository: Repository
    @Mock private lateinit var clock: Clock
    @Mock private lateinit var systemClock: SystemClockWrapper
    private val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var ifNotesViewModel: IFNotesViewModel

    @Before
    fun setUp() {
        whenever(repository.getMostRecentEatingLog())
            .thenReturn(Flowable.fromArray(Optional.absent()))
        ifNotesViewModel = createIfNotesViewModel()
    }

    @Test
    fun timeSinceLastActivity_initValueIsNull() {
        assertThat(ifNotesViewModel.timeSinceLastActivity.value, `is`(nullValue()))
    }

    @Suppress("DeferredResultUnused")
    @Test
    fun onLogButtonClicked_initEatingLogStillBeingLoaded_noop() {
        val initEatingLogPublisher = PublishSubject.create<Optional<EatingLog>>()
        whenever(repository.getMostRecentEatingLog())
            .thenReturn(initEatingLogPublisher.toFlowable(BackpressureStrategy.BUFFER))
        whenever(clock.millis()).thenReturn(1200L)

        ifNotesViewModel = createIfNotesViewModel()

        // TODO replace with runBlockingTest and TestCoroutineScope once they're stable.
        runBlocking(testScope.coroutineContext) {
            ifNotesViewModel.onLogButtonClicked()
            verify(repository, never()).insertEatingLog(any())
            verify(repository, never()).updateEatingLogAsync(any())

            initEatingLogPublisher.onNext(Optional.of(EatingLog(startTime = 100L)))
            // TODO: Remove this delay when switching to runBlockingTest
            delay(200L)
        }

        runBlocking {
            verify(repository).updateEatingLogAsync(any())
        }
    }

    @Test
    fun onLogButtonClicked_noEatingLogInProgress_createsNewEatingLog() {
        whenever(clock.millis()).thenReturn(1200L)

        runBlocking {
            ifNotesViewModel.onLogButtonClicked()
        }

        runBlocking {
            argumentCaptor<EatingLog>().apply {
                verify(repository).insertEatingLog(capture())
                assertThat(firstValue.startTime, `is`(equalTo(1200L)))
            }
        }
    }

    @Test
    fun onLogButtonClicked_eatingLogInProgress_updatesCurrentEatingLog() {
        val eatingLogInProgress = EatingLog(id = 1, startTime = 300L)
        whenever(repository.getMostRecentEatingLog())
            .thenReturn(Flowable.fromArray(Optional.of(eatingLogInProgress)))
        ifNotesViewModel = createIfNotesViewModel()
        whenever(clock.millis()).thenReturn(1200L)

        runBlocking(testScope.coroutineContext) {
            ifNotesViewModel.onLogButtonClicked()
        }

        runBlocking {
            argumentCaptor<EatingLog>().apply {
                verify(repository).updateEatingLogAsync(capture())
                assertThat(firstValue.endTime, `is`(equalTo(1200L)))
            }
        }
    }

    @Test
    fun mostRecentLogUpdated_timeSinceLastActivityIsUpdated() {
        val initEatingLogPublisher = PublishSubject.create<Optional<EatingLog>>()
        whenever(repository.getMostRecentEatingLog())
            .thenReturn(initEatingLogPublisher.toFlowable(BackpressureStrategy.BUFFER))
        ifNotesViewModel = createIfNotesViewModel()
        whenever(clock.millis()).thenReturn(200L)
        whenever(systemClock.elapsedRealtime()).thenReturn(500L)

        initEatingLogPublisher.onNext(Optional.of(EatingLog(startTime = 100L)))

        var expectedTimeSinceLastActivityChronometerData: TimeSinceLastActivityChronometerData? = null
        runOnUiThread {
                ifNotesViewModel.timeSinceLastActivity.observeForever {
                    expectedTimeSinceLastActivityChronometerData = it
                }
        }

        assertThat(
            expectedTimeSinceLastActivityChronometerData,
            `is`(equalTo(TimeSinceLastActivityChronometerData(400L, IFNotesViewModel.DARK_RED))))
    }

    private fun createIfNotesViewModel(): IFNotesViewModel {
        return IFNotesViewModel(ApplicationProvider.getApplicationContext(), repository, clock, systemClock, testScope)
    }
}
