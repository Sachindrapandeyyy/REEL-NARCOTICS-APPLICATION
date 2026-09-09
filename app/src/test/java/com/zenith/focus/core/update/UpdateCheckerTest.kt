package com.zenith.focus.core.update

import org.junit.Assert.*
import org.junit.Test

class UpdateCheckerTest {

    private fun createManifest(
        versionCode: Int,
        packageName: String = "com.zenith.focus",
        minVersionCode: Int = 1,
        mandatory: Boolean = false,
        schemaVersion: Int = 1
    ): UpdateManifest {
        return UpdateManifest(
            schemaVersion = schemaVersion,
            packageName = packageName,
            versionCode = versionCode,
            versionName = "2.$versionCode.0",
            minimumSupportedVersionCode = minVersionCode,
            mandatory = mandatory,
            releaseDate = "2026-09-10T00:00:00Z",
            apk = ApkMetadata(
                url = "https://example.com/app.apk",
                sizeBytes = 10000000L,
                sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
            ),
            releaseNotes = listOf("Test release note")
        )
    }

    @Test
    fun testUpdateAvailableWhenRemoteVersionCodeIsGreater() {
        val checker = UpdateChecker("com.zenith.focus", currentVersionCode = 4, currentVersionName = "2.2.0")
        val manifest = createManifest(versionCode = 5)

        val result = checker.evaluate(manifest)
        assertTrue(result is CheckResult.UpdateAvailable)
        val available = result as CheckResult.UpdateAvailable
        assertEquals(5, available.manifest.versionCode)
        assertFalse(available.isMandatory)
    }

    @Test
    fun testUpToDateWhenRemoteVersionCodeIsEqual() {
        val checker = UpdateChecker("com.zenith.focus", currentVersionCode = 4, currentVersionName = "2.2.0")
        val manifest = createManifest(versionCode = 4)

        val result = checker.evaluate(manifest)
        assertTrue(result is CheckResult.UpToDate)
        val upToDate = result as CheckResult.UpToDate
        assertEquals(4, upToDate.currentVersionCode)
    }

    @Test
    fun testUpToDateWhenRemoteVersionCodeIsLower() {
        val checker = UpdateChecker("com.zenith.focus", currentVersionCode = 4, currentVersionName = "2.2.0")
        val manifest = createManifest(versionCode = 3)

        val result = checker.evaluate(manifest)
        assertTrue(result is CheckResult.UpToDate)
    }

    @Test
    fun testMandatoryUpdateWhenInstalledIsBelowMinimumSupported() {
        val checker = UpdateChecker("com.zenith.focus", currentVersionCode = 4, currentVersionName = "2.2.0")
        val manifest = createManifest(versionCode = 6, minVersionCode = 5, mandatory = false)

        val result = checker.evaluate(manifest)
        assertTrue(result is CheckResult.UpdateAvailable)
        val available = result as CheckResult.UpdateAvailable
        assertTrue("Must be marked mandatory because installed (4) < minSupported (5)", available.isMandatory)
    }

    @Test
    fun testMandatoryUpdateWhenExplicitlyFlagged() {
        val checker = UpdateChecker("com.zenith.focus", currentVersionCode = 4, currentVersionName = "2.2.0")
        val manifest = createManifest(versionCode = 5, minVersionCode = 1, mandatory = true)

        val result = checker.evaluate(manifest)
        assertTrue(result is CheckResult.UpdateAvailable)
        val available = result as CheckResult.UpdateAvailable
        assertTrue(available.isMandatory)
    }

    @Test
    fun testPackageMismatchRejection() {
        val checker = UpdateChecker("com.zenith.focus", currentVersionCode = 4, currentVersionName = "2.2.0")
        val manifest = createManifest(versionCode = 5, packageName = "com.malicious.fake")

        val result = checker.evaluate(manifest)
        assertTrue(result is CheckResult.Error)
        val error = result as CheckResult.Error
        assertTrue(error.exception is UpdateException.PackageMismatchException)
    }

    @Test
    fun testUnsupportedSchemaVersionRejection() {
        val checker = UpdateChecker("com.zenith.focus", currentVersionCode = 4, currentVersionName = "2.2.0")
        val manifest = createManifest(versionCode = 5, schemaVersion = 2)

        val result = checker.evaluate(manifest)
        assertTrue(result is CheckResult.Error)
        val error = result as CheckResult.Error
        assertTrue(error.exception is UpdateException.ManifestException)
    }
}
