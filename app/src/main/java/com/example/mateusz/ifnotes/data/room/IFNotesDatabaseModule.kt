package com.example.mateusz.ifnotes.data.room

import android.app.Application
import androidx.room.Room
import com.example.mateusz.ifnotes.data.EatingLogsLocalDataSource
import com.example.mateusz.ifnotes.domain.common.Mapper
import com.example.mateusz.ifnotes.domain.entity.EatingLog
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class IFNotesDatabaseModule {
    @Module
    companion object {

        @JvmStatic
        @Provides
        @Singleton
        fun ifNotesDataBase(application: Application): IFNotesDatabase {
            return Room.databaseBuilder(
                application.applicationContext,
                IFNotesDatabase::class.java,
                "ifnotes_database").build()
        }
    }

    @Binds
    abstract fun eatingLogsLocalDataSource(
        roomEatingLogsLocalDatabase: RoomEatingLogsLocalDatabase): EatingLogsLocalDataSource
}
