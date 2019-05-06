package com.example.mateusz.ifnotes.lib

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import java.io.BufferedWriter
import java.io.OutputStreamWriter

class BackupManager(private val context: Context) {
    fun backupLogsToFile(uri: Uri, logs: String): Job {
        return async {
            context.contentResolver.openOutputStream(uri).use {
                BufferedWriter(OutputStreamWriter(it)).use {
                    it.write(logs)
                }
            }
        }
    }
}
