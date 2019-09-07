package com.example.mateusz.ifnotes.component

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.time.Clock
import java.time.ZoneId
import javax.inject.Singleton

@Module
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun viewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @Singleton
    abstract fun context(application: Application): Context

    @Binds
    @Singleton
    abstract fun application(application: IFNotesApplication): Application
}
