package com.example.mateusz.ifnotes.eatinglogs

import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.component.AppTestModule
import com.example.mateusz.ifnotes.component.ConcurrencyModule
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.ActivityForResultsData
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.Companion.CHOOSE_CSV_LOGS_TO_IMPORT_REQUEST_CODE
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.Companion.CHOOSE_CSV_FILE_TO_EXPORT_LOGS_CODE
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.Companion.EDIT_EATING_LOG_REQUEST_CODE
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.EatingLogsItemView
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogViewModel
import com.example.mateusz.ifnotes.eatinglogs.editlog.ui.EditEatingLogActivity
import com.example.mateusz.ifnotes.lib.BackupManager
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.model.Repository
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.nhaarman.mockitokotlin2.*
import com.nhaarman.mockitokotlin2.verify
import dagger.BindsInstance
import dagger.Component
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
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
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowContentResolver
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
class EatingLogsViewModelTest {
    @Mock private lateinit var repository: Repository
    @Mock private lateinit var refreshDataObserver: Observer<Event<Unit>>
    @Mock private lateinit var startActivityForResultObserver: Observer<Event<ActivityForResultsData>>
    @Mock private lateinit var eatingLogsItemView: EatingLogsItemView
    @Mock private lateinit var backupManager: BackupManager
    @Mock private lateinit var clock: Clock

    private val testScope = TestCoroutineScope()

    @Inject lateinit var eatingLogsViewModel: EatingLogsViewModel
    @Inject lateinit var csvLogsManager: CSVLogsManager
    @Inject lateinit var context: Context
    private lateinit var shadowContentResolver: ShadowContentResolver

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val eatingLogs = listOf(
        EatingLog(
            startTime = DateTimeUtils.dateTimeToMillis(
                20, 5, 2018, 10, 50),
            endTime = DateTimeUtils.dateTimeToMillis(
                20, 5, 2018, 18, 50)),
        EatingLog(
            startTime = DateTimeUtils.dateTimeToMillis(
                21, 5, 2018, 10, 50),
            endTime = DateTimeUtils.dateTimeToMillis(
                21, 5, 2018, 18, 50)),
        EatingLog(
            startTime = DateTimeUtils.dateTimeToMillis(
                25, 5, 2018, 11, 50),
            endTime = DateTimeUtils.dateTimeToMillis(
                25, 5, 2018, 18, 50)),
        EatingLog(
            startTime = DateTimeUtils.dateTimeToMillis(
                30, 5, 2019, 10, 50),
            endTime = DateTimeUtils.dateTimeToMillis(
                30, 5, 2019, 18, 50)))

    @Before
    fun setUp() {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray())
    }

    @Test
    fun init_refreshData() {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        eatingLogsViewModel.refreshData.observeForever(refreshDataObserver)
        verify(refreshDataObserver).onChanged(any())
    }

    @Test
    fun onBindEatingLogsItemView() {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        eatingLogsViewModel.onBindEatingLogsItemView(eatingLogsItemView, 0)
        verify(eatingLogsItemView).setStartTme(eatingLogs[3].startTime)
        verify(eatingLogsItemView).setEndTime(eatingLogs[3].endTime)


        eatingLogsViewModel.onBindEatingLogsItemView(eatingLogsItemView, 3)
        verify(eatingLogsItemView).setStartTme(eatingLogs[0].startTime)
        verify(eatingLogsItemView).setEndTime(eatingLogs[0].endTime)
    }

    @Test
    fun onRemoveEatingLogItemClicked() = runBlocking<Unit> {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        testScope.runBlockingTest {
            eatingLogsViewModel.onRemoveEatingLogItemClicked(2)
        }

        verify(repository).deleteEatingLog(sortDescendingByStartTime(eatingLogs)[2])
    }

    @Test
    fun onEatingLogItemViewRecycled() {
        createEatingLogsViewModel()
        eatingLogsViewModel.onEatingLogItemViewRecycled(eatingLogsItemView)

        verify(eatingLogsItemView).clearView()
    }

    @Test
    fun onEditEatingLogItemClicked() {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()
        eatingLogsViewModel.startActivityForResult.observeForever(startActivityForResultObserver)

        eatingLogsViewModel.onEditEatingLogItemClicked(3)

        argumentCaptor<Event<ActivityForResultsData>>().apply {
            verify(startActivityForResultObserver).onChanged(capture())
            val activityForResultsData = firstValue.getContentIfNotHandled()
            assertActivityComponent(activityForResultsData, EditEatingLogActivity::class.java.name)
            assertRequestCode(activityForResultsData, EDIT_EATING_LOG_REQUEST_CODE)
            activityForResultsData?.intent?.let { intent ->
                assertThat(
                    intent.getIntExtra(EditEatingLogViewModel.EXTRA_EATING_LOG_ID, 0),
                    `is`(equalTo(sortDescendingByStartTime(eatingLogs)[3].id)))
            }
        }
    }

    @Test
    fun getEatingLogsCount() {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        assertThat(eatingLogsViewModel.getEatingLogsCount(), `is`(equalTo(eatingLogs.size)))
    }

    @Test
    fun onImportLogs() {
        createEatingLogsViewModel()
        eatingLogsViewModel.startActivityForResult.observeForever(startActivityForResultObserver)

        eatingLogsViewModel.onImportLogs()

        argumentCaptor<Event<ActivityForResultsData>>().apply {
            verify(startActivityForResultObserver).onChanged(capture())
            val activityForResultsData = firstValue.getContentIfNotHandled()
            assertThat(activityForResultsData, notNullValue())
            assertRequestCode(activityForResultsData, CHOOSE_CSV_LOGS_TO_IMPORT_REQUEST_CODE)
            activityForResultsData?.intent?.let { intent ->
                assertThat(intent.action, equalTo(Intent.ACTION_GET_CONTENT))
                assertThat(intent.categories, hasItem(Intent.CATEGORY_OPENABLE))
                assertThat(intent.type, equalTo("text/*"))

            }
        }
    }

    @Test
    fun onExportLogs() {
        createEatingLogsViewModel()
        eatingLogsViewModel.startActivityForResult.observeForever(startActivityForResultObserver)
        whenever(clock.millis()).thenReturn(DateTimeUtils.timeToMillis(25, 4, 1993))

        eatingLogsViewModel.onExportLogs()

        argumentCaptor<Event<ActivityForResultsData>>().apply {
            verify(startActivityForResultObserver).onChanged(capture())
            val activityForResultsData = firstValue.getContentIfNotHandled()
            assertThat(activityForResultsData, notNullValue())
            assertRequestCode(activityForResultsData, CHOOSE_CSV_FILE_TO_EXPORT_LOGS_CODE)
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

    @Test
    fun onActivityResult_chooseCsvLogsToImport() = runBlocking<Unit> {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()
        val csvLogsPipedOutputStream = PipedOutputStream()
        val csvLogsPipedInputStream = PipedInputStream(csvLogsPipedOutputStream)
        PrintWriter(csvLogsPipedOutputStream.bufferedWriter()).apply {
            println(csvLogsManager.createCsvFromEatingLogs(eatingLogs))
            close()
        }
        val uriString = "file:///mnt/sdcard/logs.csv"
        shadowContentResolver.registerInputStream(Uri.parse(uriString), csvLogsPipedInputStream)
        val resultIntent = Intent.parseUri(uriString, 0)

        eatingLogsViewModel.onActivityResult(
            CHOOSE_CSV_LOGS_TO_IMPORT_REQUEST_CODE, RESULT_OK, resultIntent)

        inOrder(repository) {
            verify(repository).deleteAll()
            argumentCaptor<EatingLog>().apply {
                verify(repository, times(eatingLogs.size)).insertEatingLog(capture())

                val capturedLogsIterator = allValues.iterator()
                for (eatingLog in eatingLogs) {
                    val capturedLog = capturedLogsIterator.next()
                    // TODO: replace the String comparison after adopting ThreeTenABP
                    assertThat(
                        DateTimeUtils.toDateTimeString(eatingLog.startTime),
                        equalTo(DateTimeUtils.toDateTimeString(capturedLog.startTime)))

                    assertThat(
                        DateTimeUtils.toDateTimeString(eatingLog.endTime),
                        equalTo(DateTimeUtils.toDateTimeString(capturedLog.endTime)))
                }
                assertThat(capturedLogsIterator.hasNext(), `is`(false))
            }
        }
    }

    @Test
    fun onActivityResult_chooseCsvLogsToImportAndIntentIsNull_noop() = runBlocking<Unit> {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        eatingLogsViewModel.onActivityResult(
            CHOOSE_CSV_LOGS_TO_IMPORT_REQUEST_CODE, RESULT_OK, null)

        verify(repository, never()).deleteAll()
        verify(repository, never()).insertEatingLog(any())
    }

    @Test
    fun onActivityResult_chooseCsvFileToExportLogs() = runBlocking<Unit> {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()
        val uriString = "file:///mnt/sdcard/logs.csv"
        val resultIntent = Intent.parseUri(uriString, 0)

        eatingLogsViewModel.onActivityResult(
            CHOOSE_CSV_FILE_TO_EXPORT_LOGS_CODE, RESULT_OK, resultIntent)

        verify(backupManager).backupLogsToFile(
            Uri.parse(uriString),
            csvLogsManager.createCsvFromEatingLogs(sortDescendingByStartTime(eatingLogs)))
    }

    @Test
    fun onActivityResult_chooseCsvFileToExportLogsAndIntentIsNull_noop() = runBlocking<Unit> {
        whenever(repository.getEatingLogsObservable()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        eatingLogsViewModel.onActivityResult(
            CHOOSE_CSV_FILE_TO_EXPORT_LOGS_CODE, RESULT_OK, null)

        verifyZeroInteractions(backupManager)
    }

    private fun sortDescendingByStartTime(eatingLogs: List<EatingLog>) : List<EatingLog> {
        return eatingLogs.sortedWith(
            Comparator { a, b -> compareValuesBy(b, a, { it.startTime }, { it.endTime }) })
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

    private fun createEatingLogsViewModel() {
        DaggerEatingLogsViewModelTest_TestComponent.factory().create(
            ApplicationProvider.getApplicationContext(),
            repository,
            testScope,
            backupManager,
            clock,
            TestCoroutineDispatcher()).inject(this)
        shadowContentResolver = Shadows.shadowOf(context.contentResolver)
    }

    @Singleton
    @Component(modules = [AppTestModule::class])
    interface TestComponent {
        fun inject(eatingLogsViewModelTest: EatingLogsViewModelTest)

        @Component.Factory
        interface Factory {
            fun create(@BindsInstance application: Application,
                       @BindsInstance repository: Repository,
                       @BindsInstance @MainScope mainScope: CoroutineScope,
                       @BindsInstance backupManager: BackupManager,
                       @BindsInstance clock: Clock,
                       @BindsInstance @IODispatcher ioDispatcher: CoroutineDispatcher): TestComponent
        }
    }
}
