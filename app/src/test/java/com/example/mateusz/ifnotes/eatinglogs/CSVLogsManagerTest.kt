package com.example.mateusz.ifnotes.eatinglogs

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.component.ConcurrencyModule
import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.matchers.EatingLogEqualsToDate.Companion.endsOn
import com.example.mateusz.ifnotes.matchers.EatingLogEqualsToDate.Companion.startsOn
import com.example.mateusz.ifnotes.model.data.EatingLog
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.runBlocking
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
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

@RunWith(AndroidJUnit4::class)
class CSVLogsManagerTest {

    @Inject lateinit var csvLogsManager: CSVLogsManager
    private lateinit var shadowContentResolver: ShadowContentResolver

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        DaggerCSVLogsManagerTest_TestComponent.factory().create(
            context).inject(this)
        shadowContentResolver = shadowOf(context.contentResolver)
    }

    @Test
    fun getEatingLogsFromCsv() = runBlocking<Unit> {
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


    @Singleton
    @Component(modules = [ConcurrencyModule::class])
    interface TestComponent {
        fun inject(csvLogsManagerTest: CSVLogsManagerTest)

        @Component.Factory
        interface Factory {
            fun create(@BindsInstance context: Context) : TestComponent
        }
    }
}
