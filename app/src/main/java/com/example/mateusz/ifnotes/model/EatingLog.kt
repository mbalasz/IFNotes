package com.example.mateusz.ifnotes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EatingLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: Long = -1L,
    val endTime: Long = -1L
)