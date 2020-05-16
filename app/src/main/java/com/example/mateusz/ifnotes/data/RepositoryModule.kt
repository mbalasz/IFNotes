package com.example.mateusz.ifnotes.data

import com.example.mateusz.ifnotes.domain.EatingLogsRepository
import dagger.Binds
import dagger.Module

@Module
abstract class RepositoryModule {
    @Binds
    abstract fun repository(
        eatingLogsRepositoryImpl: EatingLogsRepositoryImpl): EatingLogsRepository
}
