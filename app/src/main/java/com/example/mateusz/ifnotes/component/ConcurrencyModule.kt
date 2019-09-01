package com.example.mateusz.ifnotes.component

import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Module
abstract class ConcurrencyModule {

    @Module
    companion object {
        internal annotation class MainScope

        @JvmStatic
        @Provides
        @MainScope
        fun mainScope(): CoroutineScope {
            return kotlinx.coroutines.MainScope()
        }

        internal annotation class IODispatcher

        @JvmStatic
        @Provides
        @IODispatcher
        fun ioDispatcher(): CoroutineDispatcher {
            return Dispatchers.IO
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
