package com.zenith.focus.core.update

import java.io.File

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState

    data class Available(
        val manifest: UpdateManifest,
        val isMandatory: Boolean
    ) : UpdateState

    data class Downloading(
        val progressPercent: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : UpdateState

    object Verifying : UpdateState

    data class ReadyToInstall(
        val apkFile: File,
        val manifest: UpdateManifest
    ) : UpdateState

    object Installing : UpdateState

    data class UpToDate(
        val currentVersionCode: Int,
        val currentVersionName: String
    ) : UpdateState

    data class Failed(
        val error: UpdateException
    ) : UpdateState
}

sealed class UpdateException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class NetworkException(message: String, cause: Throwable? = null) : UpdateException(message, cause)
    class ManifestException(message: String, cause: Throwable? = null) : UpdateException(message, cause)
    class PackageMismatchException(message: String) : UpdateException(message)
    class ChecksumMismatchException(message: String) : UpdateException(message)
    class SignatureMismatchException(message: String) : UpdateException(message)
    class StorageException(message: String, cause: Throwable? = null) : UpdateException(message, cause)
    class InstallationException(message: String, cause: Throwable? = null) : UpdateException(message, cause)
    class SecurityVerificationException(message: String) : UpdateException(message)
}
