package com.example.mateusz.ifnotes.component

import com.example.mateusz.ifnotes.chart.EatingLogsChartModule
import com.example.mateusz.ifnotes.data.RepositoryModule
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsModule
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogModule
import com.example.mateusz.ifnotes.presentation.ifnotes.IFNotesModule
import com.example.mateusz.ifnotes.data.room.IFNotesDatabaseModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    ConcurrencyModule::class,
    AppModule::class,
    IFNotesModule::class,
    EatingLogsChartModule::class,
    EatingLogsModule::class,
    EditEatingLogModule::class,
    IFNotesDatabaseModule::class,
    TimeModule::class,
    RepositoryModule::class])
interface IFNotesComponent : AndroidInjector<IFNotesApplication> {

    @Component.Factory
    interface Factory : AndroidInjector.Factory<IFNotesApplication>
}
