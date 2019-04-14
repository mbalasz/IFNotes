package com.example.mateusz.ifnotes.eatinglogs.editlog

import androidx.lifecycle.ViewModel
import com.example.mateusz.ifnotes.component.ActivityScope
import com.example.mateusz.ifnotes.component.ViewModelKey
import com.example.mateusz.ifnotes.eatinglogs.editlog.ui.EditEatingLogActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
abstract class EditEatingLogModule {
    @Binds
    @IntoMap
    @Singleton
    @ViewModelKey(EditEatingLogViewModel::class)
    abstract fun editEatigLogViewModel(editEatingLogViewModel: EditEatingLogViewModel): ViewModel

    @ContributesAndroidInjector
    @ActivityScope
    abstract fun editEatingLogActivity(): EditEatingLogActivity
}