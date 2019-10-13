package com.example.mateusz.ifnotes.eatinglogs

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.component.ConcurrencyModule
import com.example.mateusz.ifnotes.lib.DateTimeUtils
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
        val csvLogsStream = javaClass.classLoader.getResourceAsStream("testLogs.txt")
        val csvLogsUri = Uri.parse("content://testLogs")
        shadowContentResolver.registerInputStream(csvLogsUri, csvLogsStream)

        val eatingLogs = csvLogsManager.getEatingLogsFromCsv(csvLogsUri)

        assertThat(eatingLogs[0], startsOn(10, 4, 2019, 10, 24))
    }

    companion object {
        fun startsOn(day: Int, month: Int, year: Int, hour: Int, minute: Int): Matcher<EatingLog> {
            return EqualsToDate(day, month, year, hour, minute, "start time") { it.startTime }
        }

        fun endsOn(day: Int, month: Int, year: Int, hour: Int, minute: Int): Matcher<EatingLog> {
            return EqualsToDate(day, month, year, hour, minute, "end time") { it.endTime }
        }
    }

    class EqualsToDate(private val day: Int,
                       private val month: Int,
                       private val year: Int,
                       private val hour: Int,
                       private val minute: Int,
                       private val propertyName: String,
                       private val extractor: (EatingLog) -> Long) : BaseMatcher<EatingLog>() {
        override fun describeTo(description: Description?) {
            description?.appendText("$propertyName of the EatingLog should be equal to $year/$month/$day $hour:$minute")
        }

        override fun matches(item: Any?): Boolean {
            return DateTimeUtils.isEqualToDate(extractor(item as EatingLog), day, month, year, hour, minute)
        }
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
