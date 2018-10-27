package com.example.mateusz.ifnotes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EatingLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val startTime: Long,
    val endTime: Long,
    val date: Long = System.currentTimeMillis()
)