package com.example.mateusz.ifnotes.component

import android.app.Application
import com.example.mateusz.ifnotes.eatinglogs.EatingLogsModule
import com.example.mateusz.ifnotes.eatinglogs.editlog.EditEatingLogModule
import com.example.mateusz.ifnotes.ifnotes.IFNotesModule
import com.example.mateusz.ifnotes.model.data.IFNotesDatabaseModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    IFNotesModule::class,
    EatingLogsModule::class,
    EditEatingLogModule::class,
    IFNotesDatabaseModule::class])
interface IFNotesComponent {

    @Component.Builder
    interface Builder {
        fun build(): IFNotesComponent

        @BindsInstance
        fun application(application: Application): Builder
    }

    fun inject(application: IFNotesApplication)
}