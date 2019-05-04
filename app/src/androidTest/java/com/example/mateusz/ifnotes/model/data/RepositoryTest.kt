package com.example.mateusz.ifnotes.model.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.lib.EatingLogValidator
import com.example.mateusz.ifnotes.model.Repository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
class RepositoryTest {
    @Inject lateinit var repository: Repository
    private val eatingLogValidator = mock<EatingLogValidator>()

    @Before
    fun setUp() {
        whenever(eatingLogValidator.validateNewEatingLog(any(), any()))
                .thenReturn(EatingLogValidator.EatingLogValidationStatus.SUCCESS)
        DaggerRepositoryTest_TestComponent.builder()
                .application(ApplicationProvider.getApplicationContext())
                .eatingLogValidator(eatingLogValidator)
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
            repository.insertEatingLog(eatingLog).join()
            val updatedEatingLog = eatingLog.copy(endTime = 200L)
            repository.updateEatingLog(updatedEatingLog).await()
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
            repository.insertEatingLog(oldEatingLog).join()
            val updatedEatingLog = oldEatingLog.copy(endTime = 200L)
            repository.updateEatingLog(updatedEatingLog).await()
            repository.getEatingLogsObservable().test().awaitCount(1).assertValue(listOf(oldEatingLog))
        }
    }

    @Test
    fun updateEatingLog_noPreviousLogToUpdate() {
        val eatingLog = EatingLog(id = 1, startTime = 100L)
        runBlocking {
            repository.updateEatingLog(eatingLog).await()
            repository.getEatingLogsObservable().test().awaitCount(1).assertValue(emptyList())
        }
    }

    @Test
    fun insertEatingLog() {
        val eatingLog = EatingLog(id = 1, startTime = 100L)
        runBlocking {
            repository.insertEatingLog(eatingLog).join()
            repository.getEatingLogsObservable().test().awaitCount(1).assertValue(listOf(eatingLog))
        }
    }

    @Test
    fun getEatingLog() {
        val eatingLog = EatingLog(id = 1, startTime = 100L)
        val eatingLogTwo = EatingLog(id = 2, startTime = 200L)
        runBlocking {
            repository.insertEatingLog(eatingLog).join()
            repository.insertEatingLog(eatingLogTwo).join()
            assertThat(repository.getEatingLog(2), equalTo(eatingLogTwo))
        }
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
        }
    }
}