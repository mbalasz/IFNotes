package com.example.mateusz.ifnotes.component

import android.app.Application
import com.example.mateusz.ifnotes.ifnotes.IFNotesModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [ActivityBuilder::class, AndroidInjectionModule::class, AppModule::class, IFNotesModule::class])
interface IFNotesComponent {

    @Component.Builder
    interface Builder {
        fun build(): IFNotesComponent

        @BindsInstance
        fun application(application: Application): Builder
    }

    fun inject(application: IFNotesApplication)
}