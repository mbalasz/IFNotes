package com.example.mateusz.ifnotes.data.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EatingLogData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @Embedded(prefix = "start_time_")
    val startTime: LogDateData? = null,
    @Embedded(prefix = "end_time_")
    val endTime: LogDateData? = null)
