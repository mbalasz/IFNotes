package com.example.mateusz.ifnotes.component

import dagger.Module
import dagger.Provides
import java.time.Clock
import java.time.ZoneId
import javax.inject.Singleton

@Module
class TimeModule {

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
