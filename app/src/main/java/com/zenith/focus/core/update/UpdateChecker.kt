package com.zenith.focus.core.update

sealed interface CheckResult {
    data class UpdateAvailable(
        val manifest: UpdateManifest,
        val isMandatory: Boolean
    ) : CheckResult

    data class UpToDate(
        val currentVersionCode: Int,
        val currentVersionName: String
    ) : CheckResult

    data class Error(
        val exception: UpdateException
    ) : CheckResult
}

class UpdateChecker(
    private val expectedPackageName: String,
    private val currentVersionCode: Int,
    private val currentVersionName: String
) {
    /**
     * Determines whether an update is available based on remote manifest vs installed version.
     */
    fun evaluate(manifest: UpdateManifest): CheckResult {
        // Validate package name and manifest structure
        val validation = manifest.validate(expectedPackageName)
        if (validation.isFailure) {
            val ex = validation.exceptionOrNull() as? UpdateException
                ?: UpdateException.ManifestException("Manifest validation failed")
            return CheckResult.Error(ex)
        }

        // Compare versionCode
        return if (manifest.versionCode > currentVersionCode) {
            val isMandatory = manifest.mandatory || (currentVersionCode < manifest.minimumSupportedVersionCode)
            CheckResult.UpdateAvailable(manifest, isMandatory)
        } else {
            CheckResult.UpToDate(currentVersionCode, currentVersionName)
        }
    }
}
