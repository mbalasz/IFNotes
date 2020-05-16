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
        @Retention(AnnotationRetention.RUNTIME)
        internal annotation class MainScope

        @Provides
        @MainScope
        fun mainScope(): CoroutineScope {
            return kotlinx.coroutines.MainScope()
        }

        @Retention(AnnotationRetention.RUNTIME)
        internal annotation class IODispatcher

        @Provides
        @IODispatcher
        fun ioDispatcher(): CoroutineDispatcher {
            return Dispatchers.IO
        }


        @Retention(AnnotationRetention.RUNTIME)
        internal annotation class MainScheduler

        @Provides
        @MainScheduler
        fun mainScheduler(): Scheduler {
            return AndroidSchedulers.mainThread()
        }
    }
}
