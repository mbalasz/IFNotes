package com.example.mateusz.ifnotes.chart

import androidx.lifecycle.ViewModel
import com.example.mateusz.ifnotes.chart.ui.EatingLogsChartActivity
import com.example.mateusz.ifnotes.component.ActivityScope
import com.example.mateusz.ifnotes.component.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class EatingLogsChartModule {
    @Binds
    @IntoMap
    @ViewModelKey(EatingLogsChartViewModel::class)
    abstract fun eatingLogsChartViewModel(eatingLogsChartViewModel: EatingLogsChartViewModel): ViewModel

    @ContributesAndroidInjector
    @ActivityScope
    abstract fun eatingLogsChartActivity(): EatingLogsChartActivity
}
