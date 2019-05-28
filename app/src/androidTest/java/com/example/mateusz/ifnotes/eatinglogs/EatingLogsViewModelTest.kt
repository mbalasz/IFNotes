package com.example.mateusz.ifnotes.eatinglogs

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.ActivityForResultsData
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.Companion.CHOOSE_CSV_LOGS_REQUEST_CODE
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.Companion.CHOOSE_DIR_TO_EXPORT_CSV_CODE
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.Companion.EDIT_EATING_LOG_REQUEST_CODE
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.EatingLogsItemView
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogViewModel
import com.example.mateusz.ifnotes.eatinglogs.editlog.ui.EditEatingLogActivity
import com.example.mateusz.ifnotes.lib.BackupManager
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.time.Clock

@RunWith(AndroidJUnit4::class)
class EatingLogsViewModelTest {
    @Mock private lateinit var repository: Repository
    @Mock private lateinit var refreshDataObserver: Observer<Event<Unit>>
    @Mock private lateinit var startActivityForResultObserver: Observer<Event<ActivityForResultsData>>
    @Mock private lateinit var eatingLogsItemView: EatingLogsItemView
    @Mock private lateinit var csvLogsManager: CSVLogsManager
    @Mock private lateinit var backupManager: BackupManager
    @Mock private lateinit var clock: Clock

    private val testScope = TestCoroutineScope()

    lateinit var eatingLogsViewModel: EatingLogsViewModel

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val eatingLogs = listOf(
        EatingLog(id = 1, startTime = 100, endTime = 300),
        EatingLog(id = 2, startTime = 50, endTime = 70),
        EatingLog(id = 3, startTime = 320, endTime = 400),
        EatingLog(id = 4, startTime = 700, endTime = 800),
        EatingLog(id = 5, startTime = 500, endTime = 600))

    @Before
    fun setUp() {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray())
        eatingLogsViewModel = createEatingLogsViewModel()
    }

    @Test
    fun init_refreshData() {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        eatingLogsViewModel = createEatingLogsViewModel()

        eatingLogsViewModel.refreshData.observeForever(refreshDataObserver)
        verify(refreshDataObserver).onChanged(any())
    }

    @Test
    fun onBindEatingLogsItemView() {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        eatingLogsViewModel = createEatingLogsViewModel()

        eatingLogsViewModel.onBindEatingLogsItemView(eatingLogsItemView, 1)
        verify(eatingLogsItemView).setStartTme(500)
        verify(eatingLogsItemView).setEndTime(600)


        eatingLogsViewModel.onBindEatingLogsItemView(eatingLogsItemView, 4)
        verify(eatingLogsItemView).setStartTme(50)
        verify(eatingLogsItemView).setEndTime(70)
    }

    @Test
    fun onRemoveEatingLogItemClicked() = runBlocking<Unit> {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        eatingLogsViewModel = createEatingLogsViewModel()

        testScope.runBlockingTest {
            eatingLogsViewModel.onRemoveEatingLogItemClicked(2)
        }

        verify(repository).deleteEatingLog(eq(EatingLog(id = 3, startTime = 320, endTime = 400)))
    }

    @Test
    fun onEatingLogItemViewRecycled() {
        eatingLogsViewModel.onEatingLogItemViewRecycled(eatingLogsItemView)

        verify(eatingLogsItemView).clearView()
    }

    @Test
    fun onEditEatingLogItemClicked() {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        eatingLogsViewModel = createEatingLogsViewModel()
        eatingLogsViewModel.startActivityForResult.observeForever(startActivityForResultObserver)

        eatingLogsViewModel.onEditEatingLogItemClicked(4)

        argumentCaptor<Event<ActivityForResultsData>>().apply {
            verify(startActivityForResultObserver).onChanged(capture())
            val activityForResultsData = firstValue.getContentIfNotHandled()
            assertActivityComponent(activityForResultsData, EditEatingLogActivity::class.java.name)
            assertRequestCode(activityForResultsData, EDIT_EATING_LOG_REQUEST_CODE)
            activityForResultsData?.intent?.let { intent ->
                assertThat(
                    intent.getIntExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, 0),
                    `is`(equalTo(2)))
            }
        }
    }

    @Test
    fun getEatingLogsCount() {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        eatingLogsViewModel = createEatingLogsViewModel()

        assertThat(eatingLogsViewModel.getEatingLogsCount(), `is`(equalTo(eatingLogs.size)))
    }

    @Test
    fun onImportLogs() {
        eatingLogsViewModel.startActivityForResult.observeForever(startActivityForResultObserver)

        eatingLogsViewModel.onImportLogs()

        argumentCaptor<Event<ActivityForResultsData>>().apply {
            verify(startActivityForResultObserver).onChanged(capture())
            val activityForResultsData = firstValue.getContentIfNotHandled()
            assertThat(activityForResultsData, notNullValue())
            assertRequestCode(activityForResultsData, CHOOSE_CSV_LOGS_REQUEST_CODE)
            activityForResultsData?.intent?.let { intent ->
                assertThat(intent.action, equalTo(Intent.ACTION_GET_CONTENT))
                assertThat(intent.categories, hasItem(Intent.CATEGORY_OPENABLE))
                assertThat(intent.type, equalTo("text/*"))

            }
        }
    }

    @Test
    fun onExportLogs() {
        eatingLogsViewModel.startActivityForResult.observeForever(startActivityForResultObserver)
        whenever(clock.millis()).thenReturn(DateTimeUtils.timeToMillis(25, 4, 1993))

        eatingLogsViewModel.onExportLogs()

        argumentCaptor<Event<ActivityForResultsData>>().apply {
            verify(startActivityForResultObserver).onChanged(capture())
            val activityForResultsData = firstValue.getContentIfNotHandled()
            assertThat(activityForResultsData, notNullValue())
            assertRequestCode(activityForResultsData, CHOOSE_DIR_TO_EXPORT_CSV_CODE)
            activityForResultsData?.intent?.let { intent ->
                assertThat(intent.action, equalTo(Intent.ACTION_CREATE_DOCUMENT))
                assertThat(intent.categories, hasItem(Intent.CATEGORY_OPENABLE))
                assertThat(intent.type, equalTo("text/*"))
                assertThat(
                    intent.getStringExtra(Intent.EXTRA_TITLE),
                    equalTo("eating_logs_25/05/1993.csv"))
            }
        }
    }

    private fun assertActivityComponent(activityForResultsData: ActivityForResultsData?, className: String) {
        activityForResultsData?.intent?.component?.let {
            assertThat(it.className, equalTo(className))
        } ?: run {
            throw AssertionError("Component is null")
        }
    }

    private fun assertRequestCode(activityForResultsData: ActivityForResultsData?, requestCode: Int) {
        activityForResultsData?.let {
            assertThat(it.requestCode, equalTo(requestCode))
        } ?: run {
            throw AssertionError("ActivityForResultsData is null")
        }
    }

    private fun createEatingLogsViewModel(): EatingLogsViewModel {
        return EatingLogsViewModel(
            ApplicationProvider.getApplicationContext(),
            repository,
            testScope,
            csvLogsManager,
            backupManager,
            clock)
    }
}
