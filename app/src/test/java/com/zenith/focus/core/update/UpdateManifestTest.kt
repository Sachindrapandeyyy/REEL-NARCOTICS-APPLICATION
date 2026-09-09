package com.zenith.focus.core.update

import org.junit.Assert.*
import org.junit.Test

class UpdateManifestTest {

    private val validJson = """
        {
          "schemaVersion": 1,
          "packageName": "com.zenith.focus",
          "versionCode": 5,
          "versionName": "2.3.0",
          "minimumSupportedVersionCode": 4,
          "mandatory": false,
          "releaseDate": "2026-09-10T12:00:00Z",
          "apk": {
            "url": "https://reel-narcotics-releases.public.blob.vercel-storage.com/releases/2.3.0/reel-narcotics-2.3.0.apk",
            "sizeBytes": 11747741,
            "sha256": "9dc4f5de70debcc1e0e897dd521361553688af427da7953bf0566834a3fcdd12"
          },
          "releaseNotes": [
            "Added automated update checking",
            "Improved Scandinavian Kinfolk theme"
          ]
        }
    """.trimIndent()

    @Test
    fun testValidManifestParsing() {
        val manifest = UpdateManifest.fromJson(validJson)
        assertEquals(1, manifest.schemaVersion)
        assertEquals("com.zenith.focus", manifest.packageName)
        assertEquals(5, manifest.versionCode)
        assertEquals("2.3.0", manifest.versionName)
        assertEquals(4, manifest.minimumSupportedVersionCode)
        assertFalse(manifest.mandatory)
        assertEquals("2026-09-10T12:00:00Z", manifest.releaseDate)
        assertEquals("https://reel-narcotics-releases.public.blob.vercel-storage.com/releases/2.3.0/reel-narcotics-2.3.0.apk", manifest.apk.url)
        assertEquals(11747741L, manifest.apk.sizeBytes)
        assertEquals("9dc4f5de70debcc1e0e897dd521361553688af427da7953bf0566834a3fcdd12", manifest.apk.sha256)
        assertEquals(2, manifest.releaseNotes.size)

        val validation = manifest.validate("com.zenith.focus")
        assertTrue("Manifest should be valid", validation.isSuccess)
    }

    @Test
    fun testPackageMismatchRejection() {
        val manifest = UpdateManifest.fromJson(validJson)
        val validation = manifest.validate("com.other.fakeapp")
        assertTrue(validation.isFailure)
        assertTrue(validation.exceptionOrNull() is UpdateException.PackageMismatchException)
    }

    @Test
    fun testUnsupportedSchemaVersionRejection() {
        val invalidSchemaJson = validJson.replace("\"schemaVersion\": 1", "\"schemaVersion\": 99")
        val manifest = UpdateManifest.fromJson(invalidSchemaJson)
        val validation = manifest.validate("com.zenith.focus")
        assertTrue(validation.isFailure)
        assertTrue(validation.exceptionOrNull() is UpdateException.ManifestException)
    }

    @Test
    fun testInsecureHttpUrlRejection() {
        val insecureJson = validJson.replace("https://", "http://")
        val manifest = UpdateManifest.fromJson(insecureJson)
        val validation = manifest.validate("com.zenith.focus")
        assertTrue(validation.isFailure)
        assertTrue(validation.exceptionOrNull() is UpdateException.ManifestException)
    }

    @Test
    fun testInvalidSha256Rejection() {
        val badShaJson = validJson.replace("9dc4f5de70debcc1e0e897dd521361553688af427da7953bf0566834a3fcdd12", "short-invalid-sha")
        val manifest = UpdateManifest.fromJson(badShaJson)
        val validation = manifest.validate("com.zenith.focus")
        assertTrue(validation.isFailure)
        assertTrue(validation.exceptionOrNull() is UpdateException.ManifestException)
    }

    @Test
    fun testJsonRoundTripSerialization() {
        val original = UpdateManifest.fromJson(validJson)
        val serializedJson = original.toJson()
        val deserialized = UpdateManifest.fromJson(serializedJson)
        assertEquals(original, deserialized)
    }
}
