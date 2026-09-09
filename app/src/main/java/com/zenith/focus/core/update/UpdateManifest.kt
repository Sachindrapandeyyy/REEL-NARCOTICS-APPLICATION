package com.zenith.focus.core.update

import org.json.JSONArray
import org.json.JSONObject

data class ApkMetadata(
    val url: String,
    val sizeBytes: Long,
    val sha256: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("url", url)
        put("sizeBytes", sizeBytes)
        put("sha256", sha256)
    }

    companion object {
        fun fromJson(json: JSONObject): ApkMetadata {
            return ApkMetadata(
                url = json.getString("url"),
                sizeBytes = json.getLong("sizeBytes"),
                sha256 = json.getString("sha256")
            )
        }
    }
}

data class UpdateManifest(
    val schemaVersion: Int,
    val packageName: String,
    val versionCode: Int,
    val versionName: String,
    val minimumSupportedVersionCode: Int,
    val mandatory: Boolean,
    val releaseDate: String,
    val apk: ApkMetadata,
    val releaseNotes: List<String>
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("schemaVersion", schemaVersion)
        root.put("packageName", packageName)
        root.put("versionCode", versionCode)
        root.put("versionName", versionName)
        root.put("minimumSupportedVersionCode", minimumSupportedVersionCode)
        root.put("mandatory", mandatory)
        root.put("releaseDate", releaseDate)
        root.put("apk", apk.toJson())

        val notesArray = JSONArray()
        releaseNotes.forEach { notesArray.put(it) }
        root.put("releaseNotes", notesArray)

        return root.toString(2)
    }

    /**
     * Validates manifest structure and package identity.
     */
    fun validate(expectedPackageName: String): Result<Unit> {
        if (schemaVersion != 1) {
            return Result.failure(
                UpdateException.ManifestException("Unsupported manifest schemaVersion: $schemaVersion (expected 1)")
            )
        }
        if (packageName != expectedPackageName) {
            return Result.failure(
                UpdateException.PackageMismatchException("Package name mismatch: remote='$packageName', expected='$expectedPackageName'")
            )
        }
        if (versionCode <= 0) {
            return Result.failure(
                UpdateException.ManifestException("Invalid versionCode: $versionCode")
            )
        }
        if (!apk.url.startsWith("https://")) {
            return Result.failure(
                UpdateException.ManifestException("APK URL must use secure HTTPS transport: ${apk.url}")
            )
        }
        if (apk.sizeBytes <= 0) {
            return Result.failure(
                UpdateException.ManifestException("Invalid APK sizeBytes: ${apk.sizeBytes}")
            )
        }
        val cleanSha = apk.sha256.trim().lowercase()
        if (cleanSha.length != 64 || !cleanSha.all { it in '0'..'9' || it in 'a'..'f' }) {
            return Result.failure(
                UpdateException.ManifestException("Invalid SHA-256 hex string: ${apk.sha256}")
            )
        }
        return Result.success(Unit)
    }

    companion object {
        fun fromJson(jsonString: String): UpdateManifest {
            val root = JSONObject(jsonString)
            val schemaVersion = root.optInt("schemaVersion", -1)
            val packageName = root.getString("packageName")
            val versionCode = root.getInt("versionCode")
            val versionName = root.getString("versionName")
            val minSupportedCode = root.optInt("minimumSupportedVersionCode", 1)
            val mandatory = root.optBoolean("mandatory", false)
            val releaseDate = root.optString("releaseDate", "")

            val apkObject = root.getJSONObject("apk")
            val apk = ApkMetadata.fromJson(apkObject)

            val notesArray = root.optJSONArray("releaseNotes")
            val releaseNotes = mutableListOf<String>()
            if (notesArray != null) {
                for (i in 0 until notesArray.length()) {
                    releaseNotes.add(notesArray.getString(i))
                }
            }

            return UpdateManifest(
                schemaVersion = schemaVersion,
                packageName = packageName,
                versionCode = versionCode,
                versionName = versionName,
                minimumSupportedVersionCode = minSupportedCode,
                mandatory = mandatory,
                releaseDate = releaseDate,
                apk = apk,
                releaseNotes = releaseNotes
            )
        }
    }
}
