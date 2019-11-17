package com.example.mateusz.ifnotes.component

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class AppTestModule {
    @Binds
    @Singleton
    abstract fun context(application: Application): Context
}
