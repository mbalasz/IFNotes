package com.example.mateusz.ifnotes.component

import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScheduler
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.MainScope
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsModule
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogModule
import com.example.mateusz.ifnotes.ifnotes.IFNotesModule
import com.example.mateusz.ifnotes.model.Repository
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import io.reactivex.Scheduler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.time.Clock
import javax.inject.Singleton

// TODO: Create components explicitly in tests in order to avoid the need of providing a union set
// of dependencies for all tests.
@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    IFNotesDatabaseTestModule::class,
    EditEatingLogModule::class,
    EatingLogsModule::class,
    IFNotesModule::class,
    AppModule::class
])
interface TestComponent : AndroidInjector<IFNotesApplication> {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance ifNotesApplication: IFNotesApplication,
            @BindsInstance @MainScope coroutineScope: CoroutineScope,
            @BindsInstance @IODispatcher ioDispatcher: CoroutineDispatcher,
            @BindsInstance @MainScheduler mainScheduler: Scheduler,
            @BindsInstance clock: Clock): TestComponent
    }

    fun repository(): Repository
}

