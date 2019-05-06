package com.example.mateusz.ifnotes.component

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

    @Module
    companion object {

        @JvmStatic
        @Provides
        @Singleton
        fun clock(): Clock {
            return Clock.system(ZoneId.systemDefault())
        }
    }
}
