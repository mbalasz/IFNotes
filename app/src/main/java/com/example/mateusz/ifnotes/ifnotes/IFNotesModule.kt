package com.example.mateusz.ifnotes.ifnotes

import androidx.lifecycle.ViewModel
import com.example.mateusz.ifnotes.component.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
abstract class IFNotesModule {
    @Binds
    @IntoMap
    @ViewModelKey(IFNotesViewModel::class)
    @Singleton
    abstract fun ifNotesViewModel(ifNotesViewModel: IFNotesViewModel): ViewModel
}