package com.example.mateusz.ifnotes.data.room

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
    suspend fun insert(eatingLogData: EatingLogData)

    @Update
    suspend fun update(eatingLogData: EatingLogData)

    @Delete
    fun delete(eatingLogData: EatingLogData)

    @Query("DELETE FROM eatingLogData")
    fun deleteAll()

    @Query("SELECT * FROM eatingLogData where start_time_dateTimeInMillis = (SELECT max(start_time_dateTimeInMillis) FROM eatinglogData)")
    fun observeMostRecentEatingLog(): Flowable<List<EatingLogData>>

    @Query("SELECT * FROM eatingLogData where start_time_dateTimeInMillis = (SELECT max(start_time_dateTimeInMillis) FROM eatinglogData)")
    suspend fun getMostRecentEatingLog(): EatingLogData?

    @Query("SELECT * FROM eatingLogData")
    fun getEatingLogsFlowable(): Flowable<List<EatingLogData>>

    @Query("SELECT * FROM eatingLogData")
    fun getEatingLogs(): List<EatingLogData>

    @Query("SELECT * FROM eatingLogData WHERE id = :id")
    fun getEatingLog(id: Int): EatingLogData?
}
