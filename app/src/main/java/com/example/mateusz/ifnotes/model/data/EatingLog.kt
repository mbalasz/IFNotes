package com.example.mateusz.ifnotes.model.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EatingLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @Embedded(prefix = "start_time_")
    val startTime: LogDate? = null,
    @Embedded(prefix = "end_time_")
    val endTime: LogDate? = null)
