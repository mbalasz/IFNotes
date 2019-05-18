package com.example.mateusz.ifnotes.ifnotes

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.ifnotes.IFNotesViewModel.TimeSinceLastActivityChronometerData
import com.example.mateusz.ifnotes.lib.SystemClockWrapper
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.google.common.base.Optional
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
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
import org.reactivestreams.Publisher
import java.time.Clock
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class IFNotesViewModelTest {
    @Mock private lateinit var repository: Repository
    @Mock private lateinit var clock: Clock
    @Mock private lateinit var systemClock: SystemClockWrapper

    @get:Rule
    val mockitoRule = MockitoJUnit.rule().silent()

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

    @Test
    fun onLogButtonClicked_initEatingLogStillBeingLoaded_noop() {
        val initEatingLogPublisher = PublishSubject.create<Optional<EatingLog>>()
        whenever(repository.getMostRecentEatingLog())
            .thenReturn(initEatingLogPublisher.toFlowable(BackpressureStrategy.BUFFER))

        ifNotesViewModel = createIfNotesViewModel()
        lateinit var onLogButtonClickedJob: Job

        runBlocking {
            // TODO: mock CommonPool to a controlled pool. Otherwise, we have no guarantee that
            // after onLogButtonClicked call returns the thread is suspended on the channel and this
            // test is not useful.
            onLogButtonClickedJob = ifNotesViewModel.onLogButtonClicked()
            assertThat(onLogButtonClickedJob.isCompleted, `is`(false))
        }

        initEatingLogPublisher.onNext(Optional.of(EatingLog()))
        runBlocking {
            onLogButtonClickedJob.join()
            assertThat(onLogButtonClickedJob.isCompleted, `is`(true))
        }

    }

    @Test
    fun onLogButtonClicked_noEatingLogInProgress_createsNewEatingLog() {
        whenever(clock.millis()).thenReturn(1200L)

        runBlocking {
            ifNotesViewModel.onLogButtonClicked().join()
        }

        argumentCaptor<EatingLog>().apply {
            verify(repository).insertEatingLog(capture())
            assertThat(firstValue.startTime, `is`(equalTo(1200L)))
        }
    }

    @Test
    fun onLogButtonClicked_eatingLogInProgress_updatesCurrentEatingLog() {
        val eatingLogInProgress = EatingLog(id = 1, startTime = 300L)
        whenever(repository.getMostRecentEatingLog())
            .thenReturn(Flowable.fromArray(Optional.of(eatingLogInProgress)))
        ifNotesViewModel = createIfNotesViewModel()
        whenever(clock.millis()).thenReturn(1200L)

        runBlocking {
            ifNotesViewModel.onLogButtonClicked().join()
        }

        argumentCaptor<EatingLog>().apply {
            verify(repository).updateEatingLog(capture())
            assertThat(firstValue.endTime, `is`(equalTo(1200L)))
        }
    }

    @Test
    fun onLogButtonClicked_timeSinceLastActivityIsUpdated() {
        whenever(clock.millis()).thenReturn(1200L)

        runBlocking {
            ifNotesViewModel.onLogButtonClicked().join()
        }

        whenever(clock.millis()).thenReturn(1300L)
        whenever(systemClock.elapsedRealtime()).thenReturn(500L)

        var expectedTimeSinceLastActivityChronometerData: TimeSinceLastActivityChronometerData? = null
        runBlocking {
            async(UI) {
                ifNotesViewModel.timeSinceLastActivity.observeForever {
                    expectedTimeSinceLastActivityChronometerData = it
                }
            }.await()
        }

        assertThat(
            expectedTimeSinceLastActivityChronometerData,
            `is`(equalTo(TimeSinceLastActivityChronometerData(400L, IFNotesViewModel.DARK_RED))))
    }

    private fun createIfNotesViewModel(): IFNotesViewModel {
        return IFNotesViewModel(ApplicationProvider.getApplicationContext(), repository, clock, systemClock)
    }
}
