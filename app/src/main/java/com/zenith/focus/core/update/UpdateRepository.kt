package com.zenith.focus.core.update

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private val Context.updateDataStore: DataStore<Preferences> by preferencesDataStore(name = "zenith_update_preferences")

interface UpdateRepository {
    suspend fun fetchLatestManifest(manifestUrl: String = UpdateConfig.DEFAULT_MANIFEST_URL): Result<UpdateManifest>
    suspend fun getLastCheckedTime(): Long
    suspend fun saveLastCheckedTime(timestamp: Long)
}

class UpdateRepositoryImpl(
    private val context: Context,
    private val config: UpdateConfig = UpdateConfig()
) : UpdateRepository {

    companion object {
        private val KEY_LAST_CHECKED = longPreferencesKey("update_last_checked_timestamp")
    }

    override suspend fun fetchLatestManifest(manifestUrl: String): Result<UpdateManifest> = withContext(Dispatchers.IO) {
        var connection: HttpsURLConnection? = null
        try {
            val url = URL(manifestUrl)
            if (!url.protocol.equals("https", ignoreCase = true)) {
                return@withContext Result.failure(
                    UpdateException.NetworkException("Manifest URL must use HTTPS transport")
                )
            }

            connection = (url.openConnection() as? HttpsURLConnection)
                ?: return@withContext Result.failure(
                    UpdateException.NetworkException("Only secure HTTPS connections are permitted")
                )

            connection.connectTimeout = config.connectTimeoutMs
            connection.readTimeout = config.readTimeoutMs
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Cache-Control", "no-cache")
            connection.instanceFollowRedirects = true

            connection.connect()
            val code = connection.responseCode
            if (code != HttpsURLConnection.HTTP_OK) {
                return@withContext Result.failure(
                    UpdateException.NetworkException("Server returned HTTP $code while fetching update manifest")
                )
            }

            val body = connection.inputStream.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }

            val manifest = runCatching {
                UpdateManifest.fromJson(body)
            }.getOrElse { ex ->
                return@withContext Result.failure(
                    UpdateException.ManifestException("Failed to parse update manifest JSON: ${ex.localizedMessage}", ex)
                )
            }

            saveLastCheckedTime(System.currentTimeMillis())
            Result.success(manifest)
        } catch (e: Exception) {
            Result.failure(UpdateException.NetworkException("Network error while checking for updates: ${e.localizedMessage}", e))
        } finally {
            connection?.disconnect()
        }
    }

    override suspend fun getLastCheckedTime(): Long {
        return try {
            val prefs = context.updateDataStore.data.first()
            prefs[KEY_LAST_CHECKED] ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    override suspend fun saveLastCheckedTime(timestamp: Long) {
        try {
            context.updateDataStore.edit { prefs ->
                prefs[KEY_LAST_CHECKED] = timestamp
            }
        } catch (_: Exception) {}
    }
}
