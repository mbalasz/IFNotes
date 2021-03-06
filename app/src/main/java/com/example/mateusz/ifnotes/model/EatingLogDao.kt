package com.example.mateusz.ifnotes.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface EatingLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(eatingLog: EatingLog)

    @Update
    fun update(eatingLog: EatingLog)

    @Delete
    fun delete(eatingLog: EatingLog)

    @Query("SELECT * FROM eatingLog where date = :date")
    fun getEatingLogByDate(date: Long): EatingLog

    @Query("SELECT * FROM eatingLog where date = (SELECT max(date) FROM eatinglog)")
    fun getMostRecentEatingLog(): LiveData<EatingLog>

    @Query("SELECT * FROM eatingLog")
    fun getEatingLogs(): List<EatingLog>
}