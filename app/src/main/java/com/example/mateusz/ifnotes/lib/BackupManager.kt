package com.example.mateusz.ifnotes.lib

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.OutputStreamWriter

class BackupManager(private val context: Context) {
    suspend fun backupLogsToFile(uri: Uri, logs: String) = coroutineScope {
        launch(Dispatchers.Default) {
            context.contentResolver.openOutputStream(uri).use {
                BufferedWriter(OutputStreamWriter(it)).use {
                    it.write(logs)
                }
            }
        }
    }
}
