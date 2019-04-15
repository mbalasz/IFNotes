package com.example.mateusz.ifnotes.eatinglogs

import androidx.lifecycle.ViewModel
import com.example.mateusz.ifnotes.component.ActivityScope
import com.example.mateusz.ifnotes.component.ViewModelKey
import com.example.mateusz.ifnotes.eatinglogs.ui.EatingLogsActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class EatingLogsModule {

    @Binds
    @IntoMap
    @ViewModelKey(EatingLogsViewModel::class)
    abstract fun eatingLogsViewModel(eatingLogsViewModel: EatingLogsViewModel): ViewModel

    @ContributesAndroidInjector
    @ActivityScope
    abstract fun eatingLogsActivity(): EatingLogsActivity
}