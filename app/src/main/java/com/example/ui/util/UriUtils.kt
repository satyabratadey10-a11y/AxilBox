package com.example.ui.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object UriUtils {
    fun getFileName(context: Context, uri: Uri?): String {
        if (uri == null) return ""
        var name = ""
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = cursor.getString(nameIndex) ?: ""
                        }
                    }
                }
            } catch (_: Exception) {
                // Fallback to last path segment
            }
        }
        if (name.isEmpty()) {
            name = uri.lastPathSegment ?: "image.bin"
            val cut = name.lastIndexOf('/')
            if (cut != -1) {
                name = name.substring(cut + 1)
            }
        }
        return name
    }

    suspend fun copyUriToCache(context: Context, uriString: String, destFileName: String): String? = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            val cacheDir = File(context.cacheDir, "user_os")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val targetFile = File(cacheDir, destFileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            targetFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
