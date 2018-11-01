package com.example.mateusz.ifnotes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EatingLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: Long = 0L,
    val endTime: Long = 0L
)