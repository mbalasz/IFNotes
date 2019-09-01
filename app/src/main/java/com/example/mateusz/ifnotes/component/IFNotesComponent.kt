package com.example.mateusz.ifnotes.component

import android.app.Application
import android.content.Context
import com.example.mateusz.ifnotes.chart.EatingLogsChartModule
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsModule
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogModule
import com.example.mateusz.ifnotes.ifnotes.IFNotesModule
import com.example.mateusz.ifnotes.model.data.IFNotesDatabaseModule
import dagger.BindsInstance
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
    IFNotesDatabaseModule::class])
interface IFNotesComponent : AndroidInjector<IFNotesApplication> {

    @Component.Factory
    interface Factory : AndroidInjector.Factory<IFNotesApplication>
}
