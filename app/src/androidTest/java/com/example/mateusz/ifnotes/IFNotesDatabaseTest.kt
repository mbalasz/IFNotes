package com.example.mateusz.ifnotes

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.model.EatingLog
import com.example.mateusz.ifnotes.model.EatingLogDao
import com.example.mateusz.ifnotes.model.IFNotesDatabase
import org.hamcrest.CoreMatchers.hasItems
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for EatingLog database.
 */
@RunWith(AndroidJUnit4::class)
class IFNotesDatabaseTest {
    lateinit var eatingLogDao: EatingLogDao
    lateinit var ifNotesDatabase: IFNotesDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ifNotesDatabase = Room.inMemoryDatabaseBuilder(context, IFNotesDatabase::class.java).build()
        eatingLogDao = ifNotesDatabase.eatingLogDao()
    }

    @Test
    fun getEatingLogs() {
        val eatingLog1 = EatingLog(startTime = 10L)
        val eatingLog2 = EatingLog(startTime = 11L)

        eatingLogDao.insert(eatingLog1)
        eatingLogDao.insert(eatingLog2)

        val eatingLogs = eatingLogDao.getEatingLogsFlowable()

        assertThat(eatingLogs.map {eatingLog -> eatingLog.startTime}, hasItems(10L, 11L))
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
                .map{it.startTime}
                .test()
                .awaitCount(1)
                .assertValue(11L)
    }
}