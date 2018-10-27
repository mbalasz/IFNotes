package com.example.mateusz.ifnotes.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [EatingLog::class], version = 1)
abstract class IFNotesDatabase : RoomDatabase() {
    abstract fun eatingLogDao(): EatingLogDao

    companion object {
        @Volatile var instance: IFNotesDatabase? = null

        fun getDatabase(context: Context): IFNotesDatabase {
            val i = instance
            if (i != null) {
                return i
            }
            synchronized(this) {
                val i2 = instance
                return if (i2 == null) {
                    val created =
                            Room.databaseBuilder(
                                    context.applicationContext,
                                    IFNotesDatabase::class.java,
                                    "ifnotes_database").build()
                    instance = created
                    created
                } else {
                    i2
                }
            }
        }
    }
}