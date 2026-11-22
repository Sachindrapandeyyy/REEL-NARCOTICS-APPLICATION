package com.zenith.focus.feature.physical

import android.content.Context
import android.nfc.NfcAdapter
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

/**
 * Free DIY Physical Blocker (NFC Card / QR Code Lock) — $59 Brick hardware alternative.
 * Turns any metro card, student ID, or printed QR code into a physical focus key.
 * Stores strictly SHA-256 hashes in DataStore; never stores raw tag UIDs or payloads.
 */
object PhysicalAnchorManager {

    const val EMERGENCY_OVERRIDE_MINUTES = 15
    const val EMERGENCY_OVERRIDE_MS = EMERGENCY_OVERRIDE_MINUTES * 60 * 1000L

    fun hashBytes(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }.lowercase(Locale.US)
    }

    fun hashString(input: String): String {
        return hashBytes(input.trim().toByteArray(StandardCharsets.UTF_8))
    }

    fun verifyNfcTag(tagId: ByteArray, registeredHash: String): Boolean {
        if (registeredHash.isBlank() || tagId.isEmpty()) return false
        val computed = hashBytes(tagId)
        return verifyNfcTag(computed, registeredHash)
    }

    fun verifyNfcTag(scannedTagHash: String, registeredHash: String): Boolean {
        if (registeredHash.isBlank() || scannedTagHash.isBlank()) return false
        return scannedTagHash.trim().equals(registeredHash.trim(), ignoreCase = true)
    }

    fun verifyQrPayload(payload: String, registeredHash: String): Boolean {
        if (registeredHash.isBlank() || payload.isBlank()) return false
        val computed = hashString(payload)
        return computed.equals(registeredHash.trim().lowercase(Locale.US), ignoreCase = true)
    }

    fun generateZenithAnchorPayload(): String {
        return "ZENITH-ANCHOR-" + UUID.randomUUID().toString().uppercase(Locale.US)
    }

    fun isNfcSupported(context: Context): Boolean {
        return runCatching {
            NfcAdapter.getDefaultAdapter(context.applicationContext) != null
        }.getOrDefault(false)
    }

    fun isNfcEnabled(context: Context): Boolean {
        return runCatching {
            val adapter = NfcAdapter.getDefaultAdapter(context.applicationContext)
            adapter != null && adapter.isEnabled
        }.getOrDefault(false)
    }
}
