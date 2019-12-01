package com.example.mateusz.ifnotes.domain.usecases

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.component.IFNotesApplication
import com.example.mateusz.ifnotes.database.IFNotesDatabaseTestModule
import com.example.mateusz.ifnotes.lib.EatingLogValidator
import com.example.mateusz.ifnotes.data.EatingLogsRepositoryImpl
import com.example.mateusz.ifnotes.data.room.EatingLogData
import com.example.mateusz.ifnotes.data.room.LogDateData
import com.google.common.base.Optional
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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
    @Inject lateinit var EatingLogsRepositoryImpl: EatingLogsRepositoryImpl
    private val eatingLogValidator = mock<EatingLogValidator>()

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        whenever(eatingLogValidator.validateNewEatingLog(any(), any()))
                .thenReturn(EatingLogValidator.EatingLogValidationStatus.SUCCESS)
        DaggerRepositoryTest_TestComponent.builder()
                .application(ApplicationProvider.getApplicationContext<IFNotesApplication>())
                .eatingLogValidator(eatingLogValidator)
                .ioDispatcher(testDispatcher)
                .build()
                .inject(this)
    }

    @Test
    fun getEatingLogsObservable_initIsEmpty() {
        EatingLogsRepositoryImpl.getEatingLogsObservable().test().awaitCount(1).assertValue(emptyList())
    }

    @Test
    fun updateEatingLog() {
        val eatingLog = EatingLogData(id = 1, startTime = LogDateData(100L))
        runBlocking {
            EatingLogsRepositoryImpl.insertEatingLog(eatingLog)
            val updatedEatingLog = eatingLog.copy(endTime = LogDateData(200L))
            EatingLogsRepositoryImpl.updateEatingLog(updatedEatingLog)
            EatingLogsRepositoryImpl.getEatingLogsObservable().test().awaitCount(1).assertValue(listOf(updatedEatingLog))
        }
    }

    @Test
    fun updateEatingLog_invalidLog_doNotUpdate() {
        whenever(eatingLogValidator.validateNewEatingLog(any(), any()))
                .thenReturn(
                        EatingLogValidator.EatingLogValidationStatus.START_TIME_LATER_THAN_END_TIME)
        val oldEatingLog = EatingLogData(id = 1, startTime = LogDateData(100L))
        runBlocking {
            EatingLogsRepositoryImpl.insertEatingLog(oldEatingLog)
            val updatedEatingLog = oldEatingLog.copy(endTime = LogDateData(200L))
            EatingLogsRepositoryImpl.updateEatingLog(updatedEatingLog)
            EatingLogsRepositoryImpl.getEatingLogsObservable().test().awaitCount(1).assertValue(listOf(oldEatingLog))
        }
    }

    @Test
    fun updateEatingLog_noPreviousLogToUpdate() {
        val eatingLog = EatingLogData(id = 1, startTime = LogDateData(100L))
        runBlocking {
            EatingLogsRepositoryImpl.updateEatingLog(eatingLog)
            EatingLogsRepositoryImpl.getEatingLogsObservable().test().awaitCount(1).assertValue(emptyList())
        }
    }

    @Test
    fun insertEatingLog() {
        val eatingLogs =
            listOf(
                EatingLogData(id = 1, startTime = LogDateData(100)),
                EatingLogData(id = 2, startTime = LogDateData(50)),
                EatingLogData(id = 3, startTime = LogDateData(150)),
                EatingLogData(id = 4, startTime = LogDateData(350)),
                EatingLogData(id = 5, startTime = LogDateData(250)),
                EatingLogData(id = 6, startTime = LogDateData(50)))
        runBlocking {
            eatingLogs.forEach {
                EatingLogsRepositoryImpl.insertEatingLog(it)
            }
            EatingLogsRepositoryImpl.getEatingLogsObservable().test().awaitCount(1).assertValue(eatingLogs)
        }
    }

    @Test
    fun getEatingLog() {
        val eatingLog = EatingLogData(id = 1, startTime = LogDateData(100L))
        val eatingLogTwo = EatingLogData(id = 2, startTime = LogDateData(200L))
        runBlocking {
            EatingLogsRepositoryImpl.insertEatingLog(eatingLog)
            EatingLogsRepositoryImpl.insertEatingLog(eatingLogTwo)
            assertThat(EatingLogsRepositoryImpl.getEatingLog(2), equalTo(eatingLogTwo))
        }
    }

    @Test
    fun getMostRecentEatingLog_init_logAbsent() {
        EatingLogsRepositoryImpl.getMostRecentEatingLog().test().awaitCount(1).assertValue(Optional.absent())
    }

    @Test
    fun getMostRecentEatingLog() {
        val eatingLogs =
            listOf(
                EatingLogData(id = 1, startTime = LogDateData(100)),
                EatingLogData(id = 2, startTime = LogDateData(50)),
                EatingLogData(id = 3, startTime = LogDateData(150)),
                EatingLogData(id = 4, startTime = LogDateData(350)),
                EatingLogData(id = 5, startTime = LogDateData(250)),
                EatingLogData(id = 6, startTime = LogDateData(50)))
        runBlocking {
            eatingLogs.forEach { EatingLogsRepositoryImpl.insertEatingLog(it) }
        }
        EatingLogsRepositoryImpl.getMostRecentEatingLog()
            .map {
                assertThat(it.isPresent, `is`(true))
                it.get().startTime!!.dateTimeInMillis
            }.test()
            .awaitCount(1)
            .assertValue(350)
    }

    @Test
    fun deleteEatingLog() {
        val eatingLog = EatingLogData(id = 0)

        runBlocking {
            EatingLogsRepositoryImpl.insertEatingLog(eatingLog)
            EatingLogsRepositoryImpl.deleteEatingLog(eatingLog)

            assertThat(EatingLogsRepositoryImpl.getEatingLog(0), `is`(nullValue()))
        }
    }

    @Test
    fun deleteAll() {
        val eatingLogs = listOf(
            EatingLogData(id = 1, startTime = LogDateData(100)),
            EatingLogData(id = 2, startTime = LogDateData(50)),
            EatingLogData(id = 3, startTime = LogDateData(150)),
            EatingLogData(id = 4, startTime = LogDateData(350)),
            EatingLogData(id = 5, startTime = LogDateData(250)),
            EatingLogData(id = 6, startTime = LogDateData(50)))

        runBlocking {
            eatingLogs.forEach {
                EatingLogsRepositoryImpl.insertEatingLog(it)
            }
            EatingLogsRepositoryImpl.deleteAll()
        }

        EatingLogsRepositoryImpl.getEatingLogsObservable().test().awaitCount(1).assertValue(emptyList())
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
            fun eatingLogValidator(eatingLogValidator: EatingLogValidator): Builder

            @BindsInstance
            fun ioDispatcher(@IODispatcher ioDispatcher: CoroutineDispatcher): Builder
        }
    }
}
