package com.example.mateusz.ifnotes.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Flowable

@Dao
interface EatingLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(eatingLog: EatingLog)

    @Update
    fun update(eatingLog: EatingLog)

    @Delete
    fun delete(eatingLog: EatingLog)

    @Query("DELETE FROM EatingLog")
    fun deleteAll()

    @Query("SELECT * FROM eatingLog where startTime = (SELECT max(startTime) FROM eatinglog)")
    fun getMostRecentEatingLog(): Flowable<List<EatingLog>>

    @Query("SELECT * FROM eatingLog")
    fun getEatingLogs(): Flowable<List<EatingLog>>

    @Query("SELECT * FROM eatingLog WHERE id = :id")
    fun getEatingLog(id: Int): EatingLog
}