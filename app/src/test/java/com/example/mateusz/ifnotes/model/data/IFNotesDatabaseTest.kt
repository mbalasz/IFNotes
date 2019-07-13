package com.example.mateusz.ifnotes.model.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unit tests for EatingLog database.
 */
@RunWith(AndroidJUnit4::class)
class IFNotesDatabaseTest {
    lateinit var eatingLogDao: EatingLogDao
    @Inject lateinit var ifNotesDatabase: IFNotesDatabase

    @Before
    fun setUp() {
        DaggerIFNotesDatabaseTest_TestComponent
                .builder()
                .application(ApplicationProvider.getApplicationContext())
                .build()
                .inject(this)
        eatingLogDao = ifNotesDatabase.eatingLogDao()
    }

    @Test
    fun getEatingLogs() {
        val eatingLog1 = EatingLog(startTime = 10L)
        val eatingLog2 = EatingLog(startTime = 11L)

        eatingLogDao.insert(eatingLog1)
        eatingLogDao.insert(eatingLog2)

        eatingLogDao.getEatingLogsFlowable().map { eatingLogs ->
            eatingLogs.map {
                it.startTime
            }
        }.test().awaitCount(1).assertValue(listOf(10L, 11L))
    }

    @Test
    fun getMostRecentEatingLog() {
        val eatingLog1 = EatingLog(startTime = 10L)
        val eatingLog2 = EatingLog(startTime = 11L)
        val eatingLog3 = EatingLog(startTime = 9L)

        eatingLogDao.insert(eatingLog1)
        eatingLogDao.insert(eatingLog2)
        eatingLogDao.insert(eatingLog3)

        eatingLogDao.getMostRecentEatingLog()
                .map { it[0].startTime }
                .test()
                .awaitCount(1)
                .assertValue(11L)
    }

    @Singleton
    @Component(modules = [
        IFNotesDatabaseTestModule::class])
    interface TestComponent {
        fun inject(iFNotesDatabaseTest: IFNotesDatabaseTest)

        @Component.Builder
        interface Builder {
            fun build(): TestComponent

            @BindsInstance
            fun application(application: Application): Builder
        }
    }
}
