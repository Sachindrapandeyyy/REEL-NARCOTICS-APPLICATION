package com.zenith.focus.core.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class UpdateManager(
    private val context: Context,
    private val repository: UpdateRepository = UpdateRepositoryImpl(context),
    private val downloader: UpdateDownloader = UpdateDownloader(context),
    private val verifier: UpdateVerifier = UpdateVerifier(context),
    private val installer: UpdateInstaller = UpdateInstaller(context),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var activeJob: Job? = null

    val currentVersionCode: Int by lazy {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            6 // Fallback matching current build
        }
    }

    val currentVersionName: String by lazy {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "2.3.1"
        } catch (e: Exception) {
            "2.3.1"
        }
    }

    private val checker = UpdateChecker(
        expectedPackageName = context.packageName,
        currentVersionCode = currentVersionCode,
        currentVersionName = currentVersionName
    )

    fun checkForUpdates(manifestUrl: String = UpdateConfig.DEFAULT_MANIFEST_URL) {
        if (_state.value is UpdateState.Checking || _state.value is UpdateState.Downloading) {
            return
        }

        activeJob?.cancel()
        activeJob = scope.launch {
            _state.value = UpdateState.Checking
            val result = repository.fetchLatestManifest(manifestUrl)

            result.fold(
                onSuccess = { manifest ->
                    when (val check = checker.evaluate(manifest)) {
                        is CheckResult.UpdateAvailable -> {
                            _state.value = UpdateState.Available(
                                manifest = check.manifest,
                                isMandatory = check.isMandatory
                            )
                        }
                        is CheckResult.UpToDate -> {
                            _state.value = UpdateState.UpToDate(
                                currentVersionCode = check.currentVersionCode,
                                currentVersionName = check.currentVersionName
                            )
                        }
                        is CheckResult.Error -> {
                            _state.value = UpdateState.Failed(check.exception)
                        }
                    }
                },
                onFailure = { error ->
                    val updateEx = (error as? UpdateException)
                        ?: UpdateException.NetworkException(error.localizedMessage ?: "Network error", error)
                    _state.value = UpdateState.Failed(updateEx)
                }
            )
        }
    }

    fun startDownload(manifest: UpdateManifest) {
        if (_state.value is UpdateState.Downloading) return

        activeJob?.cancel()
        activeJob = scope.launch {
            _state.value = UpdateState.Downloading(0, 0, manifest.apk.sizeBytes)

            try {
                var downloadedFile: File? = null

                downloader.download(manifest).collect { progress ->
                    if (progress.completedFile != null) {
                        downloadedFile = progress.completedFile
                    } else {
                        _state.value = UpdateState.Downloading(
                            progressPercent = progress.percent,
                            bytesDownloaded = progress.bytesDownloaded,
                            totalBytes = progress.totalBytes
                        )
                    }
                }

                val finalFile = downloadedFile
                if (finalFile == null || !finalFile.exists()) {
                    throw UpdateException.NetworkException("Downloaded file not found on disk")
                }

                // Verification step
                _state.value = UpdateState.Verifying
                val verifyResult = verifier.verify(finalFile, manifest)

                verifyResult.fold(
                    onSuccess = {
                        _state.value = UpdateState.ReadyToInstall(finalFile, manifest)
                    },
                    onFailure = { ex ->
                        val err = (ex as? UpdateException)
                            ?: UpdateException.SecurityVerificationException(ex.localizedMessage ?: "Verification failed")
                        _state.value = UpdateState.Failed(err)
                    }
                )

            } catch (e: Exception) {
                val err = (e as? UpdateException)
                    ?: UpdateException.NetworkException("Download failed: ${e.localizedMessage}", e)
                _state.value = UpdateState.Failed(err)
            }
        }
    }

    fun installUpdate(apkFile: File) {
        _state.value = UpdateState.Installing
        val result = installer.install(apkFile)
        result.onFailure { ex ->
            val err = (ex as? UpdateException)
                ?: UpdateException.InstallationException(ex.localizedMessage ?: "Installation failed", ex)
            _state.value = UpdateState.Failed(err)
        }
    }

    fun resetState() {
        activeJob?.cancel()
        _state.value = UpdateState.Idle
    }

    fun canRequestPackageInstalls(): Boolean = installer.canRequestPackageInstalls()
    fun getManageUnknownAppSourcesIntent() = installer.getManageUnknownAppSourcesIntent()
}
