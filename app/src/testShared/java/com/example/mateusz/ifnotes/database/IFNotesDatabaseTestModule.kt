package com.example.mateusz.ifnotes.database

import android.app.Application
import androidx.room.Room
import com.example.mateusz.ifnotes.data.EatingLogsLocalDataSource
import com.example.mateusz.ifnotes.data.room.IFNotesDatabase
import com.example.mateusz.ifnotes.data.room.RoomEatingLogsLocalDatabase
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.TestCoroutineDispatcher
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
abstract class IFNotesDatabaseTestModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        @Singleton
        fun ifNotesDataBase(application: Application, @QueryExecutor queryExecutor: Optional<Executor>): IFNotesDatabase {
            return Room.inMemoryDatabaseBuilder(application, IFNotesDatabase::class.java)
                .allowMainThreadQueries()
                // We need to set both QueryExecutor and TransactionExecutor.
                // Setting QueryExecutor to an immediate executor allows test code to synchronously
                // subscribe to Room DAO's observables. Room uses QueryExecutor for Flowable
                // schedulers.
                // However, if we set QueryExecutor but never set a custom TransactionExecutor, then
                // TransactionExecutor will by default be assigned to QueryExecutor.
                // Transaction logic gets hold of the Transaction thread by suspending the
                // transaction coroutine and executing a runnable on the TransactionExecutor where
                // it resumes the suspended coroutine. With TransactionExecutor being immediate,
                // that runnable runs synchronously on the main thread. Resuming the suspended
                // coroutine fails (I don't understand why) and the thread is held indefinitely
                // which leads to a deadlock.
                .setTransactionExecutor(Executors.newSingleThreadExecutor())
                .setQueryExecutor(queryExecutor.orElse(TestCoroutineDispatcher().asExecutor()))
                .build()
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    internal annotation class QueryExecutor

    @BindsOptionalOf
    @QueryExecutor
    abstract fun queryExecutor(): Executor

    @Binds
    abstract fun eatingLogsLocalDataSource(
        roomEatingLogsLocalDatabase: RoomEatingLogsLocalDatabase): EatingLogsLocalDataSource
}
