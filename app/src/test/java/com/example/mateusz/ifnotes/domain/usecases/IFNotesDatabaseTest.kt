package com.example.mateusz.ifnotes.domain.usecases

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.data.room.EatingLogDao
import com.example.mateusz.ifnotes.data.room.EatingLogData
import com.example.mateusz.ifnotes.data.room.IFNotesDatabase
import com.example.mateusz.ifnotes.data.room.LogDateData
import com.example.mateusz.ifnotes.database.IFNotesDatabaseTestModule
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.runBlocking
import org.junit.Before
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
        val eatingLog1 = EatingLogData(startTime = LogDateData(10L))
        val eatingLog2 = EatingLogData(startTime = LogDateData(11L))

        runBlocking {
            eatingLogDao.insert(eatingLog1)
            eatingLogDao.insert(eatingLog2)
        }

        eatingLogDao.getEatingLogsFlowable().map { eatingLogs ->
            eatingLogs.map {
                it.startTime!!.dateTimeInMillis
            }
        }.test().awaitCount(1).assertValue(listOf(10L, 11L))
    }

    @Test
    fun getMostRecentEatingLog() {
        val eatingLog1 = EatingLogData(startTime = LogDateData(10L))
        val eatingLog2 = EatingLogData(startTime = LogDateData(11L))
        val eatingLog3 = EatingLogData(startTime = LogDateData(9L))

        runBlocking {
            eatingLogDao.insert(eatingLog1)
            eatingLogDao.insert(eatingLog2)
            eatingLogDao.insert(eatingLog3)
        }

        eatingLogDao.observeMostRecentEatingLog()
                .map { it[0].startTime!!.dateTimeInMillis }
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
