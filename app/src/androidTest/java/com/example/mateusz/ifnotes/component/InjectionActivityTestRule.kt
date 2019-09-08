package com.example.mateusz.ifnotes.component

import android.app.Activity
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ActivityTestRule
import dagger.android.AndroidInjector

class InjectionActivityTestRule<T: Activity>(
    activityClass: Class<T>,
    private val component: AndroidInjector<IFNotesApplication>,
    launchActivity: Boolean = true) : ActivityTestRule<T> (activityClass, false, launchActivity) {
    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()
        val app = ApplicationProvider.getApplicationContext<IFNotesApplication>()
        app.setComponent(component)

    }
}

private fun IFNotesApplication.setComponent(component: AndroidInjector<IFNotesApplication>) {
    this.component = component.also { it.inject(this) }
}
