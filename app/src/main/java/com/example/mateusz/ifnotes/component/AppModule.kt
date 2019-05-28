package com.example.mateusz.ifnotes.component

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
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

    @Module
    companion object {

        @JvmStatic
        @Provides
        @Singleton
        fun clock(): Clock {
            return Clock.system(ZoneId.systemDefault())
        }

        internal annotation class MainScope

        @JvmStatic
        @Provides
        @MainScope
        fun mainScope(): CoroutineScope {
            return kotlinx.coroutines.MainScope()
        }


        internal annotation class MainScheduler

        @JvmStatic
        @Provides
        @MainScheduler
        fun mainScheduler(): Scheduler {
            return AndroidSchedulers.mainThread()
        }
    }
}
