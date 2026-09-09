package com.zenith.focus.core.update

data class UpdateConfig(
    val manifestUrl: String = DEFAULT_MANIFEST_URL,
    val connectTimeoutMs: Int = 10000,
    val readTimeoutMs: Int = 15000
) {
    companion object {
        const val DEFAULT_MANIFEST_URL = "https://reel-narcotics.vercel.app/latest/update.json"
    }
}
