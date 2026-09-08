package com.zenith.focus.domain.model

data class BlockEvent(
    val id: Long = 0L,
    val timestamp: Long,
    val packageName: String,
    val category: ContentCategory,
    val confidence: Float,
    val ruleId: String
)
