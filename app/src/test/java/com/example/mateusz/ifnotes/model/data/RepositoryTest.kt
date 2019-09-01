package com.example.mateusz.ifnotes.model.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.component.ConcurrencyModule
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.component.IFNotesApplication
import com.example.mateusz.ifnotes.lib.EatingLogValidator
import com.example.mateusz.ifnotes.model.Repository
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
class RepositoryTest {
    @Inject lateinit var repository: Repository
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
        repository.getEatingLogsObservable().test().awaitCount(1).assertValue(emptyList())
    }

    @Test
    fun updateEatingLog() {
        val eatingLog = EatingLog(id = 1, startTime = 100L)
        runBlocking {
            repository.insertEatingLog(eatingLog)
            val updatedEatingLog = eatingLog.copy(endTime = 200L)
            repository.updateEatingLog(updatedEatingLog)
            repository.getEatingLogsObservable().test().awaitCount(1).assertValue(listOf(updatedEatingLog))
        }
    }

    @Test
    fun updateEatingLog_invalidLog_doNotUpdate() {
        whenever(eatingLogValidator.validateNewEatingLog(any(), any()))
                .thenReturn(
                        EatingLogValidator.EatingLogValidationStatus.START_TIME_LATER_THAN_END_TIME)
        val oldEatingLog = EatingLog(id = 1, startTime = 100L)
        runBlocking {
            repository.insertEatingLog(oldEatingLog)
            val updatedEatingLog = oldEatingLog.copy(endTime = 200L)
            repository.updateEatingLog(updatedEatingLog)
            repository.getEatingLogsObservable().test().awaitCount(1).assertValue(listOf(oldEatingLog))
        }
    }

    @Test
    fun updateEatingLog_noPreviousLogToUpdate() {
        val eatingLog = EatingLog(id = 1, startTime = 100L)
        runBlocking {
            repository.updateEatingLog(eatingLog)
            repository.getEatingLogsObservable().test().awaitCount(1).assertValue(emptyList())
        }
    }

    @Test
    fun insertEatingLog() {
        val eatingLogs =
            listOf(
                EatingLog(id = 1, startTime = 100),
                EatingLog(id = 2, startTime = 50),
                EatingLog(id = 3, startTime = 150),
                EatingLog(id = 4, startTime = 350),
                EatingLog(id = 5, startTime = 250),
                EatingLog(id = 6, startTime = 50))
        runBlocking {
            eatingLogs.forEach {
                repository.insertEatingLog(it)
            }
            repository.getEatingLogsObservable().test().awaitCount(1).assertValue(eatingLogs)
        }
    }

    @Test
    fun getEatingLog() {
        val eatingLog = EatingLog(id = 1, startTime = 100L)
        val eatingLogTwo = EatingLog(id = 2, startTime = 200L)
        runBlocking {
            repository.insertEatingLog(eatingLog)
            repository.insertEatingLog(eatingLogTwo)
            assertThat(repository.getEatingLog(2), equalTo(eatingLogTwo))
        }
    }

    @Test
    fun getMostRecentEatingLog_init_logAbsent() {
        repository.getMostRecentEatingLog().test().awaitCount(1).assertValue(Optional.absent())
    }

    @Test
    fun getMostRecentEatingLog() {
        val eatingLogs =
            listOf(
                EatingLog(id = 1, startTime = 100),
                EatingLog(id = 2, startTime = 50),
                EatingLog(id = 3, startTime = 150),
                EatingLog(id = 4, startTime = 350),
                EatingLog(id = 5, startTime = 250),
                EatingLog(id = 6, startTime = 50))
        runBlocking {
            eatingLogs.forEach { repository.insertEatingLog(it) }
        }
        repository.getMostRecentEatingLog()
            .map {
                assertThat(it.isPresent, `is`(true))
                it.get().startTime
            }.test()
            .awaitCount(1)
            .assertValue(350)
    }

    @Test
    fun deleteEatingLog() {
        val eatingLog = EatingLog(id = 0)

        runBlocking {
            repository.insertEatingLog(eatingLog)
            repository.deleteEatingLog(eatingLog)

            assertThat(repository.getEatingLog(0), `is`(nullValue()))
        }
    }

    @Test
    fun deleteAll() {
        val eatingLogs = listOf(
            EatingLog(id = 1, startTime = 100),
            EatingLog(id = 2, startTime = 50),
            EatingLog(id = 3, startTime = 150),
            EatingLog(id = 4, startTime = 350),
            EatingLog(id = 5, startTime = 250),
            EatingLog(id = 6, startTime = 50))

        runBlocking {
            eatingLogs.forEach {
                repository.insertEatingLog(it)
            }
            repository.deleteAll()
        }

        repository.getEatingLogsObservable().test().awaitCount(1).assertValue(emptyList())
    }

    @Singleton
    @Component(modules = [
        IFNotesDatabaseTestModule::class])
    interface TestComponent {
        fun inject(repositoryTest: RepositoryTest)

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
