package com.example.mateusz.ifnotes.lib

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class BackupManager @Inject constructor(private val context: Context) {
    open suspend fun backupLogsToFile(uri: Uri, logs: String) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri).use {
            BufferedWriter(OutputStreamWriter(it)).use {
                it.write(logs)
            }
        }
    }
}
