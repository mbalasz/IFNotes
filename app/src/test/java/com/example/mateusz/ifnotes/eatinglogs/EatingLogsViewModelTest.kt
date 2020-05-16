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
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.database.IFNotesDatabaseTestModule
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import com.example.mateusz.ifnotes.domain.usecases.DeleteAllEatingLogs
import com.example.mateusz.ifnotes.domain.usecases.DeleteEatingLog
import com.example.mateusz.ifnotes.domain.usecases.InsertEatingLog
import com.example.mateusz.ifnotes.domain.usecases.ObserveEatingLogs
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.ActivityForResultsData
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.Companion.CHOOSE_CSV_FILE_TO_EXPORT_LOGS_CODE
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.Companion.CHOOSE_CSV_LOGS_TO_IMPORT_REQUEST_CODE
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.Companion.EDIT_EATING_LOG_REQUEST_CODE
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsViewModel.EatingLogsItemView
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogViewModel
import com.example.mateusz.ifnotes.eatinglogs.editlog.ui.EditEatingLogActivity
import com.example.mateusz.ifnotes.lib.BackupManager
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.lib.Event
import com.example.mateusz.ifnotes.livedata.testObserve
import com.nhaarman.mockitokotlin2.*
import dagger.BindsInstance
import dagger.Component
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
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
    @Mock private lateinit var refreshDataObserver: Observer<Event<Unit>>
    @Mock private lateinit var startActivityForResultObserver: Observer<Event<ActivityForResultsData>>
    @Mock private lateinit var eatingLogsItemView: EatingLogsItemView
    @Mock private lateinit var backupManager: BackupManager
    @Mock private lateinit var clock: Clock
    @Mock private lateinit var deleteAllEatingLogs: DeleteAllEatingLogs
    @Mock private lateinit var insertEatingLog: InsertEatingLog
    @Mock private lateinit var deleteEatingLog: DeleteEatingLog
    @Mock private lateinit var observeEatingLogs: ObserveEatingLogs

    private val testScope = TestCoroutineScope()

    @Inject lateinit var eatingLogsViewModel: EatingLogsViewModel
    @Inject lateinit var csvLogsManager: CSVLogsManager
    @Inject lateinit var context: Context
    private lateinit var shadowContentResolver: ShadowContentResolver

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val eatingLogs = listOf(
        EatingLog(
            startTime = LogDate(DateTimeUtils.dateTimeToMillis(
                20, 5, 2018, 10, 50)),
            endTime = LogDate(DateTimeUtils.dateTimeToMillis(
                20, 5, 2018, 18, 50))),
        EatingLog(
            startTime = LogDate(DateTimeUtils.dateTimeToMillis(
                21, 5, 2018, 10, 50)),
            endTime = LogDate(DateTimeUtils.dateTimeToMillis(
                21, 5, 2018, 18, 50))),
        EatingLog(
            startTime = LogDate(DateTimeUtils.dateTimeToMillis(
                25, 5, 2018, 11, 50)),
            endTime = LogDate(DateTimeUtils.dateTimeToMillis(
                25, 5, 2018, 18, 50))),
        EatingLog(
            startTime = LogDate(DateTimeUtils.dateTimeToMillis(
                30, 5, 2019, 10, 50)),
            endTime = LogDate(DateTimeUtils.dateTimeToMillis(
                30, 5, 2019, 18, 50))))

    @Before
    fun setUp() {
        whenever(observeEatingLogs()).thenReturn(Flowable.fromArray())
    }

    @Test
    fun init_refreshData() {
        whenever(observeEatingLogs()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        eatingLogsViewModel.refreshData.observeForever(refreshDataObserver)
        verify(refreshDataObserver).onChanged(any())
    }

    @Test
    fun onBindEatingLogsItemView() {
        whenever(observeEatingLogs()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        eatingLogsViewModel.onBindEatingLogsItemView(eatingLogsItemView, 0)
        verify(eatingLogsItemView).setStartTme(eatingLogs[3].startTime!!.dateTimeInMillis)
        verify(eatingLogsItemView).setEndTime(eatingLogs[3].endTime!!.dateTimeInMillis)


        eatingLogsViewModel.onBindEatingLogsItemView(eatingLogsItemView, 3)
        verify(eatingLogsItemView).setStartTme(eatingLogs[0].startTime!!.dateTimeInMillis)
        verify(eatingLogsItemView).setEndTime(eatingLogs[0].endTime!!.dateTimeInMillis)
    }

    @Test
    fun onRemoveEatingLogItemClicked() {
        whenever(observeEatingLogs()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        testScope.runBlockingTest {
            eatingLogsViewModel.onRemoveEatingLogItemClicked(2)
            verify(deleteEatingLog).invoke(sortDescendingByStartTime(eatingLogs)[2])
        }
    }

    @Test
    fun onEatingLogItemViewRecycled() {
        createEatingLogsViewModel()
        eatingLogsViewModel.onEatingLogItemViewRecycled(eatingLogsItemView)

        verify(eatingLogsItemView).clearView()
    }

    @Test
    fun onEditEatingLogItemClicked() {
        whenever(observeEatingLogs()).thenReturn(Flowable.fromArray(eatingLogs))
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
        whenever(observeEatingLogs()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        assertThat(eatingLogsViewModel.getEatingLogsCount(), `is`(equalTo(eatingLogs.size)))
    }

    @Test
    fun onImportLogs() {
        createEatingLogsViewModel()
        eatingLogsViewModel.startActivityForResult.observeForever(startActivityForResultObserver)

        eatingLogsViewModel.onImportLogs()

        val activityForResultsData = eatingLogsViewModel.startActivityForResult.testObserve().getContentIfNotHandled()
        assertThat(activityForResultsData, notNullValue())
        assertRequestCode(activityForResultsData, CHOOSE_CSV_LOGS_TO_IMPORT_REQUEST_CODE)
        activityForResultsData?.intent?.let { intent ->
            assertThat(intent.action, equalTo(Intent.ACTION_GET_CONTENT))
            assertThat(intent.categories, hasItem(Intent.CATEGORY_OPENABLE))
            assertThat(intent.type, equalTo("text/*"))

        }
    }

    @Test
    fun onExportLogs() {
        createEatingLogsViewModel()
        whenever(clock.millis()).thenReturn(DateTimeUtils.timeToMillis(25, 4, 1993))

        eatingLogsViewModel.onExportLogs()

        val activityForResultsData = eatingLogsViewModel.startActivityForResult.testObserve().getContentIfNotHandled()
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

    @Test
    fun onActivityResult_chooseCsvLogsToImport() = testScope.runBlockingTest {
        whenever(observeEatingLogs()).thenReturn(Flowable.fromArray(eatingLogs))
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

        inOrder(deleteAllEatingLogs, insertEatingLog) {
            verify(deleteAllEatingLogs).invoke()
            argumentCaptor<EatingLog>().apply {
                verify(insertEatingLog, times(eatingLogs.size)).invoke(capture())

                val capturedLogsIterator = allValues.iterator()
                for (eatingLog in eatingLogs) {
                    val capturedLog = capturedLogsIterator.next()
                    // TODO: replace the String comparison after adopting ThreeTenABP
                    assertThat(
                        DateTimeUtils.toDateTimeString(eatingLog.startTime!!.dateTimeInMillis),
                        equalTo(DateTimeUtils.toDateTimeString(capturedLog.startTime!!.dateTimeInMillis)))

                    assertThat(
                        DateTimeUtils.toDateTimeString(eatingLog.endTime!!.dateTimeInMillis),
                        equalTo(DateTimeUtils.toDateTimeString(capturedLog.endTime!!.dateTimeInMillis)))
                }
                assertThat(capturedLogsIterator.hasNext(), `is`(false))
            }
        }
    }

    @Test
    fun onActivityResult_chooseCsvLogsToImportAndIntentIsNull_noop() = testScope.runBlockingTest {
        whenever(observeEatingLogs()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        eatingLogsViewModel.onActivityResult(
            CHOOSE_CSV_LOGS_TO_IMPORT_REQUEST_CODE, RESULT_OK, null)

        verifyZeroInteractions(deleteAllEatingLogs, insertEatingLog)
    }

    @Test
    fun onActivityResult_chooseCsvFileToExportLogs() = testScope.runBlockingTest {
        whenever(observeEatingLogs()).thenReturn(Flowable.fromArray(eatingLogs))
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
    fun onActivityResult_chooseCsvFileToExportLogsAndIntentIsNull_noop() = testScope.runBlockingTest {
        whenever(observeEatingLogs()).thenReturn(Flowable.fromArray(eatingLogs))
        createEatingLogsViewModel()

        eatingLogsViewModel.onActivityResult(
            CHOOSE_CSV_FILE_TO_EXPORT_LOGS_CODE, RESULT_OK, null)

        verifyZeroInteractions(backupManager)
    }

    private fun sortDescendingByStartTime(EatingLog: List<EatingLog>) : List<EatingLog> {
        return EatingLog.sortedWith(
            Comparator { a, b -> compareValuesBy(b, a, { it.startTime?.dateTimeInMillis }, { it.endTime?.dateTimeInMillis }) })
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
            testScope,
            backupManager,
            clock,
            TestCoroutineDispatcher(),
            deleteAllEatingLogs,
            insertEatingLog,
            deleteEatingLog,
            observeEatingLogs).inject(this)
        shadowContentResolver = Shadows.shadowOf(context.contentResolver)
    }

    @Singleton
    @Component(modules = [AppTestModule::class, IFNotesDatabaseTestModule::class])
    interface TestComponent {
        fun inject(eatingLogsViewModelTest: EatingLogsViewModelTest)

        @Component.Factory
        interface Factory {
            fun create(@BindsInstance application: Application,
                       @BindsInstance @MainScope mainScope: CoroutineScope,
                       @BindsInstance backupManager: BackupManager,
                       @BindsInstance clock: Clock,
                       @BindsInstance @IODispatcher ioDispatcher: CoroutineDispatcher,
                       @BindsInstance deleteAllEatingLogs: DeleteAllEatingLogs,
                       @BindsInstance insertEatingLog: InsertEatingLog,
                       @BindsInstance deleteEatingLog: DeleteEatingLog,
                       @BindsInstance observeEatingLogs: ObserveEatingLogs): TestComponent
        }
    }
}
