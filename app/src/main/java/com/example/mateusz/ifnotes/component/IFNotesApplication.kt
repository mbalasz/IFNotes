package com.example.mateusz.ifnotes.component

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class IFNotesApplication : DaggerApplication() {

    lateinit var component: AndroidInjector<out DaggerApplication>

    override fun onCreate() {
        component = DaggerIFNotesComponent.factory().create(this)
        super.onCreate()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return component
    }
}
