package com.example.mateusz.ifnotes.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [EatingLogData::class], version = 1)
abstract class IFNotesDatabase : RoomDatabase() {
    abstract fun eatingLogDao(): EatingLogDao
}
