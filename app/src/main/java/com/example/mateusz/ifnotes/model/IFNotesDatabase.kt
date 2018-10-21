package com.example.mateusz.ifnotes.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [EatingLog::class], version = 1)
abstract class IFNotesDatabase : RoomDatabase() {
    abstract fun eatingLogDao(): EatingLogDao
}