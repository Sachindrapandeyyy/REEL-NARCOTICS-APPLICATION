package com.zenith.focus.accessibility.overlay

import com.zenith.focus.domain.model.ContentCategory

sealed class OverlayState {
    object Idle : OverlayState()
    data class Active(
        val category: ContentCategory,
        val reason: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : OverlayState()
}
