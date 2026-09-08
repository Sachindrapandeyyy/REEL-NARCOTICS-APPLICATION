package com.zenith.focus.domain.repository

import com.zenith.focus.domain.model.BlockEvent
import com.zenith.focus.domain.model.ContentCategory
import kotlinx.coroutines.flow.Flow

data class DailyStat(
    val dayTimestamp: Long,
    val totalBlocks: Int,
    val shortsBlocks: Int,
    val reelsBlocks: Int,
    val spotlightBlocks: Int,
    val adultBlocks: Int,
    val estimatedFocusMinutesSaved: Int
)

interface StatisticsRepository {
    suspend fun recordBlockEvent(event: BlockEvent)
    fun getRecentEvents(limit: Int = 100): Flow<List<BlockEvent>>
    fun getDailyStats(days: Int = 7): Flow<List<DailyStat>>
    suspend fun getTodayBlockCount(): Int
    suspend fun getTodayCountByCategory(category: ContentCategory): Int
    suspend fun getFocusStreakDays(): Int
    suspend fun clearAllStatistics()
    suspend fun exportStatisticsCsv(): String
}
