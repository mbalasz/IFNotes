package com.example.mateusz.ifnotes.domain.usecases

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.component.IFNotesApplication
import com.example.mateusz.ifnotes.data.EatingLogsRepositoryImpl
import com.example.mateusz.ifnotes.database.IFNotesDatabaseTestModule
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import com.example.mateusz.ifnotes.domain.entity.LogDate
import com.google.common.base.Optional
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton

// TODO: replace runBlocking with runBlockingTest once
// https://github.com/Kotlin/kotlinx.coroutines/pull/1206 is merged.
@RunWith(AndroidJUnit4::class)
class EatingLogsRepositoryImplTest {
    @Inject lateinit var eatingLogsRepositoryImpl: EatingLogsRepositoryImpl

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        DaggerEatingLogsRepositoryImplTest_TestComponent.builder()
                .application(ApplicationProvider.getApplicationContext<IFNotesApplication>())
                .ioDispatcher(testDispatcher)
                .build()
                .inject(this)
    }

    @Test
    fun getEatingLogsObservable_initIsEmpty() {
        eatingLogsRepositoryImpl.observeEatingLogs().test().awaitCount(1).assertValue(emptyList())
    }

    @Test
    fun updateEatingLog() {
        val eatingLog = EatingLog(id = 1, startTime = LogDate(100L))
        runBlocking {
            eatingLogsRepositoryImpl.insertEatingLog(eatingLog)
            val updatedEatingLog = eatingLog.copy(endTime = LogDate(200L))
            eatingLogsRepositoryImpl.updateEatingLog(updatedEatingLog)
            eatingLogsRepositoryImpl
                .observeEatingLogs()
                .test()
                .awaitCount(1)
                .assertValue(listOf(updatedEatingLog))
        }
    }

    @Test
    fun insertEatingLog() {
        val eatingLogs =
            listOf(
                EatingLog(id = 1, startTime = LogDate(100)),
                EatingLog(id = 2, startTime = LogDate(50)),
                EatingLog(id = 3, startTime = LogDate(150)),
                EatingLog(id = 4, startTime = LogDate(350)),
                EatingLog(id = 5, startTime = LogDate(250)),
                EatingLog(id = 6, startTime = LogDate(50)))
        runBlocking {
            eatingLogs.forEach {
                eatingLogsRepositoryImpl.insertEatingLog(it)
            }
            eatingLogsRepositoryImpl.observeEatingLogs()
                .test()
                .awaitCount(1)
                .assertValue(eatingLogs)
        }
    }

    @Test
    fun getEatingLog() {
        val eatingLog = EatingLog(id = 1, startTime = LogDate(100L))
        val eatingLogTwo = EatingLog(id = 2, startTime = LogDate(200L))
        runBlocking {
            eatingLogsRepositoryImpl.insertEatingLog(eatingLog)
            eatingLogsRepositoryImpl.insertEatingLog(eatingLogTwo)
            assertThat(eatingLogsRepositoryImpl.getEatingLog(2), equalTo(eatingLogTwo))
        }
    }

    @Test
    fun getMostRecentEatingLog_init_logAbsent() {
        eatingLogsRepositoryImpl.observeMostRecentEatingLog().test().awaitCount(1).assertValue(Optional.absent())
    }

    @Test
    fun getMostRecentEatingLog() {
        val eatingLogs =
            listOf(
                EatingLog(id = 1, startTime = LogDate(100)),
                EatingLog(id = 2, startTime = LogDate(50)),
                EatingLog(id = 3, startTime = LogDate(150)),
                EatingLog(id = 4, startTime = LogDate(350)),
                EatingLog(id = 5, startTime = LogDate(250)),
                EatingLog(id = 6, startTime = LogDate(50)))
        runBlocking {
            eatingLogs.forEach { eatingLogsRepositoryImpl.insertEatingLog(it) }
        }
        eatingLogsRepositoryImpl.observeMostRecentEatingLog()
            .map {
                assertThat(it.isPresent, `is`(true))
                it.get().startTime!!.dateTimeInMillis
            }.test()
            .awaitCount(1)
            .assertValue(350)
    }

    @Test
    fun deleteEatingLog() {
        val eatingLog = EatingLog(id = 0)

        runBlocking {
            eatingLogsRepositoryImpl.insertEatingLog(eatingLog)
            eatingLogsRepositoryImpl.deleteEatingLog(eatingLog)

            assertThat(eatingLogsRepositoryImpl.getEatingLog(0), `is`(nullValue()))
        }
    }

    @Test
    fun deleteAll() {
        val eatingLogs = listOf(
            EatingLog(id = 1, startTime = LogDate(100)),
            EatingLog(id = 2, startTime = LogDate(50)),
            EatingLog(id = 3, startTime = LogDate(150)),
            EatingLog(id = 4, startTime = LogDate(350)),
            EatingLog(id = 5, startTime = LogDate(250)),
            EatingLog(id = 6, startTime = LogDate(50)))

        runBlocking {
            eatingLogs.forEach {
                eatingLogsRepositoryImpl.insertEatingLog(it)
            }
            eatingLogsRepositoryImpl.deleteAllEatingLogs()
        }

        eatingLogsRepositoryImpl.observeEatingLogs().test().awaitCount(1).assertValue(emptyList())
    }

    @Singleton
    @Component(modules = [
        IFNotesDatabaseTestModule::class])
    interface TestComponent {
        fun inject(repositoryTest: EatingLogsRepositoryImplTest)

        @Component.Builder
        interface Builder {
            fun build(): TestComponent

            @BindsInstance
            fun application(application: Application): Builder

            @BindsInstance
            fun ioDispatcher(@IODispatcher ioDispatcher: CoroutineDispatcher): Builder
        }
    }
}
