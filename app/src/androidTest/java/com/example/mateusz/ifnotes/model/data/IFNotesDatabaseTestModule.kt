package com.example.mateusz.ifnotes.model.data

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class IFNotesDatabaseTestModule {

    @Provides
    @Singleton
    fun ifNotesDataBase(application: Application): IFNotesDatabase {
        return Room.inMemoryDatabaseBuilder(application, IFNotesDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}