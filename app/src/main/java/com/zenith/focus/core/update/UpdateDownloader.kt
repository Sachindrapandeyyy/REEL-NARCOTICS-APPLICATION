package com.zenith.focus.core.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.coroutineContext

data class DownloadProgress(
    val percent: Int,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val completedFile: File? = null
)

class UpdateDownloader(
    private val context: Context
) {
    /**
     * Downloads APK from the given manifest with real-time progress.
     * Uses atomic temporary file naming and ensures cleanup on failure.
     */
    fun download(manifest: UpdateManifest): Flow<DownloadProgress> = flow {
        val updatesDir = File(context.cacheDir, "updates")
        if (!updatesDir.exists()) {
            updatesDir.mkdirs()
        }

        // Verify storage space
        val freeSpace = updatesDir.usableSpace
        val requiredSpace = manifest.apk.sizeBytes + (5 * 1024 * 1024) // 5MB margin
        if (freeSpace < requiredSpace) {
            throw UpdateException.StorageException(
                "Insufficient storage: requires ${requiredSpace / (1024 * 1024)}MB, available ${freeSpace / (1024 * 1024)}MB"
            )
        }

        val tempFile = File(updatesDir, "update_${manifest.versionCode}.apk.tmp")
        val finalFile = File(updatesDir, "update_${manifest.versionCode}.apk")

        // Clean any leftover files for this or previous update downloads to free up cache space
        runCatching {
            updatesDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".apk") || file.name.endsWith(".tmp")) {
                    file.delete()
                }
            }
        }

        val url = URL(manifest.apk.url)
        val connection = (url.openConnection() as? HttpsURLConnection)
            ?: throw UpdateException.NetworkException("Only secure HTTPS connections are permitted")

        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("Accept-Encoding", "identity")

        try {
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw UpdateException.NetworkException("Server responded with HTTP $responseCode")
            }

            val expectedSize = if (manifest.apk.sizeBytes > 0) manifest.apk.sizeBytes else connection.contentLength.toLong()
            var bytesDownloaded = 0L

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    var lastReportedPercent = -1

                    while (input.read(buffer).also { read = it } != -1) {
                        if (!coroutineContext.isActive) {
                            throw UpdateException.NetworkException("Download cancelled")
                        }
                        output.write(buffer, 0, read)
                        bytesDownloaded += read

                        val percent = if (expectedSize > 0) {
                            ((bytesDownloaded * 100) / expectedSize).toInt().coerceIn(0, 100)
                        } else 0

                        if (percent != lastReportedPercent) {
                            lastReportedPercent = percent
                            emit(DownloadProgress(percent, bytesDownloaded, expectedSize))
                        }
                    }
                    output.flush()
                }
            }

            if (bytesDownloaded < manifest.apk.sizeBytes) {
                tempFile.delete()
                throw UpdateException.NetworkException(
                    "Incomplete download: received $bytesDownloaded of ${manifest.apk.sizeBytes} bytes"
                )
            }

            // Atomic rename to final file
            if (!tempFile.renameTo(finalFile)) {
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }

            emit(DownloadProgress(100, bytesDownloaded, expectedSize, completedFile = finalFile))

        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            if (finalFile.exists()) finalFile.delete()
            if (e is UpdateException) throw e
            throw UpdateException.NetworkException("Failed to download APK: ${e.localizedMessage}", e)
        } finally {
            connection.disconnect()
        }
    }.flowOn(Dispatchers.IO)
}
