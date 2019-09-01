package com.example.mateusz.ifnotes.lib

import android.content.Context
import android.net.Uri
import com.example.mateusz.ifnotes.component.ConcurrencyModule.Companion.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class BackupManager @Inject constructor(
    private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) {
    open suspend fun backupLogsToFile(uri: Uri, logs: String) = withContext(ioDispatcher) {
        context.contentResolver.openOutputStream(uri).use {
            BufferedWriter(OutputStreamWriter(it)).use {
                it.write(logs)
            }
        }
    }
}
