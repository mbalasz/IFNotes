package com.example.mateusz.ifnotes.eatinglogs

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.matchers.EatingLogEqualsToDate.Companion.endsOn
import com.example.mateusz.ifnotes.matchers.EatingLogEqualsToDate.Companion.startsOn
import com.example.mateusz.ifnotes.model.data.EatingLog
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowContentResolver
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class CSVLogsManagerTest {

    @Inject lateinit var csvLogsManager: CSVLogsManager
    private lateinit var shadowContentResolver: ShadowContentResolver
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        DaggerCSVLogsManagerTest_TestComponent.factory().create(
            context, testDispatcher).inject(this)
        shadowContentResolver = shadowOf(context.contentResolver)
    }

    @Test
    fun getEatingLogsFromCsv() = testDispatcher.runBlockingTest {
        val csvLogsPipedOutputStream = PipedOutputStream()
        val csvLogsPipedInputStream = PipedInputStream(csvLogsPipedOutputStream)
        PrintWriter(csvLogsPipedOutputStream.bufferedWriter()).apply {
            println("Headers")
            println("2019-05-10,10:24,2019-05-10,19:50")
            println("2019-05-11,10:24,2019-05-11,19:50")
            close()
        }
        val csvLogsUri = Uri.parse("content://testLogs")
        shadowContentResolver.registerInputStream(csvLogsUri, csvLogsPipedInputStream)

        val eatingLogs = csvLogsManager.getEatingLogsFromCsv(csvLogsUri)

        assertThat(eatingLogs[0], startsOn(10, 4, 2019, 10, 24))
        assertThat(eatingLogs[0], endsOn(10, 4, 2019, 19, 50))

        assertThat(eatingLogs[1], startsOn(11, 4, 2019, 10, 24))
        assertThat(eatingLogs[1], endsOn(11, 4, 2019, 19, 50))
    }

    @Test
    fun getEatingLogsFromCsv_emptyLine() = testDispatcher.runBlockingTest {
        val csvLogsPipedOutputStream = PipedOutputStream()
        val csvLogsPipedInputStream = PipedInputStream(csvLogsPipedOutputStream)
        PrintWriter(csvLogsPipedOutputStream.bufferedWriter()).apply {
            println("Headers")
            println("2019-05-10,10:24,2019-05-10,19:50")
            println("")
            println("2019-05-11,10:24,2019-05-11,19:50")
            close()
        }
        val csvLogsUri = Uri.parse("content://testLogs")
        shadowContentResolver.registerInputStream(csvLogsUri, csvLogsPipedInputStream)

        val eatingLogs = csvLogsManager.getEatingLogsFromCsv(csvLogsUri)

        assertThat(eatingLogs[0], startsOn(10, 4, 2019, 10, 24))
        assertThat(eatingLogs[0], endsOn(10, 4, 2019, 19, 50))

        assertThat(eatingLogs[1], startsOn(11, 4, 2019, 10, 24))
        assertThat(eatingLogs[1], endsOn(11, 4, 2019, 19, 50))
    }

    @Test
    fun getEatingLogsFromCsv_dateSeparatedWithSlashes() = testDispatcher.runBlockingTest {
        val csvLogsPipedOutputStream = PipedOutputStream()
        val csvLogsPipedInputStream = PipedInputStream(csvLogsPipedOutputStream)
        PrintWriter(csvLogsPipedOutputStream.bufferedWriter()).apply {
            println("Headers")
            println("2019/05/10,10:24,2019/05/10,19:50")
            println("2019/05/11,10:24,2019/05/11,19:50")
            close()
        }
        val csvLogsUri = Uri.parse("content://testLogs")
        shadowContentResolver.registerInputStream(csvLogsUri, csvLogsPipedInputStream)

        val eatingLogs = csvLogsManager.getEatingLogsFromCsv(csvLogsUri)

        assertThat(eatingLogs[0], startsOn(10, 4, 2019, 10, 24))
        assertThat(eatingLogs[0], endsOn(10, 4, 2019, 19, 50))

        assertThat(eatingLogs[1], startsOn(11, 4, 2019, 10, 24))
        assertThat(eatingLogs[1], endsOn(11, 4, 2019, 19, 50))
    }

    @Test
    fun getEatingLogsFromCsv_noEndTime() = testDispatcher.runBlockingTest {
        val csvLogsPipedOutputStream = PipedOutputStream()
        val csvLogsPipedInputStream = PipedInputStream(csvLogsPipedOutputStream)
        PrintWriter(csvLogsPipedOutputStream.bufferedWriter()).apply {
            println("Headers")
            println("2019/05/10,10:24,2019/05/10,19:50")
            println("2019/05/11,10:24")
            println("2019/05/12,12:24,")
            close()
        }
        val csvLogsUri = Uri.parse("content://testLogs")
        shadowContentResolver.registerInputStream(csvLogsUri, csvLogsPipedInputStream)

        val eatingLogs = csvLogsManager.getEatingLogsFromCsv(csvLogsUri)

        assertThat(eatingLogs[0], startsOn(10, 4, 2019, 10, 24))
        assertThat(eatingLogs[0], endsOn(10, 4, 2019, 19, 50))

        assertThat(eatingLogs[1], startsOn(11, 4, 2019, 10, 24))
        assertThat(eatingLogs[2], startsOn(12, 4, 2019, 12, 24))
    }

    @Test
    fun getEatingLogsFromCsv_invalidEndTime_failsWithException() {
        val csvLogsPipedOutputStream = PipedOutputStream()
        val csvLogsPipedInputStream = PipedInputStream(csvLogsPipedOutputStream)
        PrintWriter(csvLogsPipedOutputStream.bufferedWriter()).apply {
            println("Headers")
            println("2019/05/10,10:24,2019/05/10,19:50")
            println("2019/05/10,10:24,2019/05/10,1950")
            println("2019/05/11,10:24,2019/05/11,19:50")
            close()
        }
        val csvLogsUri = Uri.parse("content://testLogs")
        shadowContentResolver.registerInputStream(csvLogsUri, csvLogsPipedInputStream)

        assertFailsWith(IllegalStateException::class) {
            testDispatcher.runBlockingTest {
                csvLogsManager.getEatingLogsFromCsv(csvLogsUri)
            }
        }
    }

    @Test
    fun getEatingLogsFromCsv_invalidStartTime_failsWithException() {
        val csvLogsPipedOutputStream = PipedOutputStream()
        val csvLogsPipedInputStream = PipedInputStream(csvLogsPipedOutputStream)
        PrintWriter(csvLogsPipedOutputStream.bufferedWriter()).apply {
            println("Headers")
            println("2019/05/10,10:24,2019/05/10,19:50")
            println("2019/05/10,1024,2019/05/10,19:50")
            println("2019/05/11,10:24,2019/05/11,19:50")
            close()
        }
        val csvLogsUri = Uri.parse("content://testLogs")
        shadowContentResolver.registerInputStream(csvLogsUri, csvLogsPipedInputStream)

        assertFailsWith(IllegalStateException::class) {
            testDispatcher.runBlockingTest {
                csvLogsManager.getEatingLogsFromCsv(csvLogsUri)
            }
        }
    }

    @Test
    fun getEatingLogsFromCsv_corruptedLine_failsWithException() {
        val csvLogsPipedOutputStream = PipedOutputStream()
        val csvLogsPipedInputStream = PipedInputStream(csvLogsPipedOutputStream)
        PrintWriter(csvLogsPipedOutputStream.bufferedWriter()).apply {
            println("Headers")
            println("2019/05/10,10:24,2019/05/10,19:50")
            println("random line")
            println("2019/05/11,10:24,2019/05/11,19:50")
            close()
        }
        val csvLogsUri = Uri.parse("content://testLogs")
        shadowContentResolver.registerInputStream(csvLogsUri, csvLogsPipedInputStream)

        assertFailsWith(IllegalStateException::class) {
            testDispatcher.runBlockingTest {
                csvLogsManager.getEatingLogsFromCsv(csvLogsUri)
            }
        }
    }

    @Test
    fun createCsvFromEatingLogs() {
        val eatingLogs = listOf(
            EatingLog(
                startTime = DateTimeUtils.dateTimeToMillis(
                    10, 4, 2019, 10, 24),
                endTime = DateTimeUtils.dateTimeToMillis(
                    10, 4, 2019, 19, 24)),
            EatingLog(
                startTime = DateTimeUtils.dateTimeToMillis(
                    11, 4, 2019, 10, 24),
                endTime = DateTimeUtils.dateTimeToMillis(
                    11, 4, 2019, 19, 24)),
            EatingLog(
                startTime = DateTimeUtils.dateTimeToMillis(
                    12, 4, 2019, 10, 24)
            ))

        val csvString = csvLogsManager.createCsvFromEatingLogs(eatingLogs)

        assertThat(csvString, equalTo(
            """
                Start date,Start time,End date,End time
                2019-05-10,10:24,2019-05-10,19:24
                2019-05-11,10:24,2019-05-11,19:24
                2019-05-12,10:24,

            """.trimIndent()
        ))
    }

    @Singleton
    @Component
    interface TestComponent {
        fun inject(csvLogsManagerTest: CSVLogsManagerTest)

        @Component.Factory
        interface Factory {
            fun create(@BindsInstance context: Context,
                       @BindsInstance @IODispatcher ioDispatcher: CoroutineDispatcher) : TestComponent
        }
    }
}
