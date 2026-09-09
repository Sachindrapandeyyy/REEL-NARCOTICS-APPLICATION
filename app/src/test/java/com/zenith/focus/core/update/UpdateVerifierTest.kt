package com.zenith.focus.core.update

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.security.MessageDigest

class UpdateVerifierTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testSha256CalculationAccuracy() {
        val testFile = tempFolder.newFile("sample_update.apk")
        val content = "Reel Narcotics Production Release Verification Content"
        testFile.writeText(content)

        // Compute expected SHA-256
        val digest = MessageDigest.getInstance("SHA-256")
        val expectedSha = digest.digest(content.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

        // Test calculation logic
        val calculatedSha = calculateFileSha256(testFile)
        assertEquals(expectedSha, calculatedSha)
    }

    @Test
    fun testEmptyFileCalculation() {
        val emptyFile = tempFolder.newFile("empty.apk")
        val sha = calculateFileSha256(emptyFile)
        // SHA-256 of empty bytes
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", sha)
    }

    private fun calculateFileSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
