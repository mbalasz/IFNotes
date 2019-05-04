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
import kotlinx.coroutines.experimental.async
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
        DaggerRepositoryTest_TestComponent.builder()
                .application(ApplicationProvider.getApplicationContext())
                .eatingLogValidator(EatingLogValidator())
                .build()
                .inject(this)
    }

    @Test
    fun getEatingLogsObservable_initIsEmpty() {
        repository.getEatingLogsObservable().test().awaitCount(1).assertValue(emptyList())
    }

    @Test
    fun updateEatingLog() {
        val eatingLog = EatingLog(startTime = 100L)
        async {
            repository.updateEatingLog(eatingLog).await()
            repository.getEatingLogsObservable().test().awaitCount(1).assertValue(listOf(eatingLog))
        }
    }

    @Test
    fun updateEatingLog_invalidLog_doNotUpdate() {
        whenever(eatingLogValidator.validateNewEatingLog(any(), any()))
                .thenReturn(
                        EatingLogValidator.EatingLogValidationStatus.START_TIME_LATER_THAN_END_TIME)
        val eatingLog = EatingLog(startTime = 100L)
        async {
            repository.updateEatingLog(eatingLog).await()
            repository.getEatingLogsObservable().test().awaitCount(1).assertValue(emptyList())
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