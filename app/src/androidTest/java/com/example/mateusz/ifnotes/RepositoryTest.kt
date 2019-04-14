package com.example.mateusz.ifnotes

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.model.Repository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryTest {
    private lateinit var repository: Repository

    @Before
    fun setUp() {
        repository = Repository(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun getEatingLogsObservable_initIsEmpty() {
        repository.getEatingLogsObservable().test().awaitCount(1).assertNoValues()
    }

    @Test
    fun updateEatingLog() {
        repository.updateEatingLog(EatingLog(startTime = 100L))
        repository.getEatingLogsObservable().test().awaitCount(1).assertNoValues()
    }
}