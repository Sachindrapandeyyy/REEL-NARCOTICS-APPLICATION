package com.zenith.focus.feature.physical

import org.junit.Assert.*
import org.junit.Test

class PhysicalAnchorManagerTest {

    @Test
    fun `hashBytes produces correct 64 character SHA-256 lowercase hex string`() {
        val testBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val hash = PhysicalAnchorManager.hashBytes(testBytes)

        assertEquals(64, hash.length)
        assertTrue(hash.all { it in '0'..'9' || it in 'a'..'f' })
        // Hashing the same bytes again produces identical hash
        val hash2 = PhysicalAnchorManager.hashBytes(testBytes)
        assertEquals(hash, hash2)
    }

    @Test
    fun `hashString produces consistent SHA-256 hex string`() {
        val payload = "ZENITH-ANCHOR-TEST-KEY-123"
        val hash = PhysicalAnchorManager.hashString(payload)

        assertEquals(64, hash.length)
        assertEquals(hash, PhysicalAnchorManager.hashString(payload))
        assertNotEquals(hash, PhysicalAnchorManager.hashString(payload + "-DIFF"))
    }

    @Test
    fun `verifyNfcTag returns true when hash matches registered hash`() {
        val rawBytes = byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte())
        val registeredHash = PhysicalAnchorManager.hashBytes(rawBytes)

        assertTrue(PhysicalAnchorManager.verifyNfcTag(registeredHash, registeredHash))
        // Case-insensitivity & whitespace trimming
        assertTrue(PhysicalAnchorManager.verifyNfcTag("  " + registeredHash.uppercase() + " ", registeredHash))
    }

    @Test
    fun `verifyNfcTag returns false on mismatch or empty registered hash`() {
        val hash1 = PhysicalAnchorManager.hashBytes(byteArrayOf(1, 2, 3))
        val hash2 = PhysicalAnchorManager.hashBytes(byteArrayOf(4, 5, 6))

        assertFalse(PhysicalAnchorManager.verifyNfcTag(hash1, hash2))
        assertFalse(PhysicalAnchorManager.verifyNfcTag(hash1, ""))
        assertFalse(PhysicalAnchorManager.verifyNfcTag("", hash1))
        assertFalse(PhysicalAnchorManager.verifyNfcTag("", ""))
    }

    @Test
    fun `verifyQrPayload correctly verifies raw payload against stored hash`() {
        val rawPayload = "ZENITH-ANCHOR-A1B2C3D4E5"
        val expectedHash = PhysicalAnchorManager.hashString(rawPayload)

        assertTrue(PhysicalAnchorManager.verifyQrPayload(rawPayload, expectedHash))
        assertTrue(PhysicalAnchorManager.verifyQrPayload("  $rawPayload  ", expectedHash))
        assertFalse(PhysicalAnchorManager.verifyQrPayload("WRONG-PAYLOAD", expectedHash))
        assertFalse(PhysicalAnchorManager.verifyQrPayload("", expectedHash))
        assertFalse(PhysicalAnchorManager.verifyQrPayload(rawPayload, ""))
    }

    @Test
    fun `generateZenithAnchorPayload generates valid prefix and high entropy`() {
        val payload1 = PhysicalAnchorManager.generateZenithAnchorPayload()
        val payload2 = PhysicalAnchorManager.generateZenithAnchorPayload()

        assertTrue(payload1.startsWith("ZENITH-ANCHOR-"))
        assertTrue(payload2.startsWith("ZENITH-ANCHOR-"))
        assertNotEquals(payload1, payload2)
        assertTrue(payload1.length > 20)
    }

    @Test
    fun `emergency override duration is 15 minutes`() {
        assertEquals(15, PhysicalAnchorManager.EMERGENCY_OVERRIDE_MINUTES)
    }
}
