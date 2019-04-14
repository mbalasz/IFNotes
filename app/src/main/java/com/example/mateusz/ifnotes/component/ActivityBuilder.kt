package com.example.mateusz.ifnotes.component

import com.example.mateusz.ifnotes.ifnotes.ui.IFNotesActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector()
    abstract fun ifNotesActivity(): IFNotesActivity
}