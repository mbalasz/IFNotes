package com.example.mateusz.ifnotes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EatingLog(
    @PrimaryKey(autoGenerate = true)
    val startTime: Int,
    val endTime: Int,
    val date: Long = System.currentTimeMillis()
)