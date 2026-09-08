package com.zenith.focus.data.repository

import com.zenith.focus.core.time.DateTimeUtils
import com.zenith.focus.data.local.ZenithDatabaseHelper
import com.zenith.focus.domain.model.BlockEvent
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.repository.DailyStat
import com.zenith.focus.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StatisticsRepositoryImpl(
    private val dbHelper: ZenithDatabaseHelper
) : StatisticsRepository {

    override suspend fun recordBlockEvent(event: BlockEvent) {
        dbHelper.insertBlockEvent(event)
    }

    override fun getRecentEvents(limit: Int): Flow<List<BlockEvent>> = flow {
        emit(dbHelper.getRecentEvents(limit))
    }

    override fun getDailyStats(days: Int): Flow<List<DailyStat>> = flow {
        emit(dbHelper.getDailyStats(days))
    }

    override suspend fun getTodayBlockCount(): Int {
        val startOfToday = DateTimeUtils.getStartOfToday()
        return dbHelper.getCountSince(startOfToday)
    }

    override suspend fun getTodayCountByCategory(category: ContentCategory): Int {
        val startOfToday = DateTimeUtils.getStartOfToday()
        return dbHelper.getCategoryCountSince(category, startOfToday)
    }

    override suspend fun getFocusStreakDays(): Int {
        return dbHelper.calculateFocusStreak()
    }

    override suspend fun clearAllStatistics() {
        dbHelper.clearAll()
    }

    override suspend fun exportStatisticsCsv(): String {
        return dbHelper.exportCsv()
    }
}
