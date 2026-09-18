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
     * Uses atomic temporary file naming, handles CDN redirects, and falls back to secondary URL if needed.
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

        val candidateUrls = listOfNotNull(
            manifest.apk.url.takeIf { it.isNotBlank() },
            manifest.apk.fallbackUrl?.takeIf { it.isNotBlank() }
        ).distinct()

        var downloadedSuccessfully = false
        var lastException: Exception? = null

        for (targetUrl in candidateUrls) {
            try {
                downloadSingleUrl(targetUrl, manifest, tempFile) { progress ->
                    emit(progress)
                }
                downloadedSuccessfully = true
                break
            } catch (e: Exception) {
                if (tempFile.exists()) tempFile.delete()
                lastException = e
            }
        }

        if (!downloadedSuccessfully) {
            if (tempFile.exists()) tempFile.delete()
            if (finalFile.exists()) finalFile.delete()
            val err = lastException
            if (err is UpdateException) throw err
            throw UpdateException.NetworkException("Failed to download APK: ${err?.localizedMessage}", err)
        }

        // Atomic rename to final file
        if (!tempFile.renameTo(finalFile)) {
            tempFile.copyTo(finalFile, overwrite = true)
            tempFile.delete()
        }

        emit(DownloadProgress(100, manifest.apk.sizeBytes, manifest.apk.sizeBytes, completedFile = finalFile))
    }.flowOn(Dispatchers.IO)

    private suspend fun downloadSingleUrl(
        urlString: String,
        manifest: UpdateManifest,
        tempFile: File,
        onProgress: suspend (DownloadProgress) -> Unit
    ) {
        var currentUrl = urlString
        var connection: HttpsURLConnection? = null
        var redirectCount = 0
        val maxRedirects = 5

        try {
            while (redirectCount < maxRedirects) {
                val parsedUrl = URL(currentUrl)
                if (!parsedUrl.protocol.equals("https", ignoreCase = true)) {
                    throw UpdateException.NetworkException("Only secure HTTPS connections are permitted")
                }

                connection = (parsedUrl.openConnection() as? HttpsURLConnection)
                    ?: throw UpdateException.NetworkException("Only secure HTTPS connections are permitted")

                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.instanceFollowRedirects = false // Manually handle 301/302/307/308
                connection.setRequestProperty("Accept-Encoding", "identity")
                connection.setRequestProperty("User-Agent", "ReelNarcotics/${context.packageName}")
                connection.connect()

                val code = connection.responseCode
                if (code in 300..399) {
                    val redirectLocation = connection.getHeaderField("Location")
                        ?: throw UpdateException.NetworkException("Server returned redirect $code without Location header")
                    currentUrl = if (redirectLocation.startsWith("http")) {
                        redirectLocation
                    } else {
                        URL(parsedUrl, redirectLocation).toString()
                    }
                    connection.disconnect()
                    redirectCount++
                    continue
                }

                if (code != HttpsURLConnection.HTTP_OK) {
                    throw UpdateException.NetworkException("Server responded with HTTP $code")
                }
                break
            }

            val finalConn = connection ?: throw UpdateException.NetworkException("Failed to establish connection")
            val expectedSize = if (manifest.apk.sizeBytes > 0) manifest.apk.sizeBytes else finalConn.contentLength.toLong()
            var bytesDownloaded = 0L

            finalConn.inputStream.use { input ->
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
                            onProgress(DownloadProgress(percent, bytesDownloaded, expectedSize))
                        }
                    }
                    output.flush()
                }
            }

            if (manifest.apk.sizeBytes > 0 && bytesDownloaded < manifest.apk.sizeBytes) {
                throw UpdateException.NetworkException(
                    "Incomplete download: received $bytesDownloaded of ${manifest.apk.sizeBytes} bytes"
                )
            }
        } finally {
            connection?.disconnect()
        }
    }
}
