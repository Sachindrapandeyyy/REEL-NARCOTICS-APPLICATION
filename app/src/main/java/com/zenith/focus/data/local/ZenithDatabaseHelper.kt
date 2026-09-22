package com.zenith.focus.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.zenith.focus.core.time.DateTimeUtils
import com.zenith.focus.domain.model.BlockEvent
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.repository.DailyStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class ZenithDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "zenith_focus.db"
        const val DATABASE_VERSION = 2

        const val TABLE_BLOCK_EVENTS = "block_events"
        const val COL_ID = "id"
        const val COL_TIMESTAMP = "timestamp"
        const val COL_PACKAGE_NAME = "package_name"
        const val COL_CATEGORY = "category"
        const val COL_CONFIDENCE = "confidence"
        const val COL_RULE_ID = "rule_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createQuery = "CREATE TABLE " + TABLE_BLOCK_EVENTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TIMESTAMP + " INTEGER NOT NULL, " +
                COL_PACKAGE_NAME + " TEXT NOT NULL, " +
                COL_CATEGORY + " TEXT NOT NULL, " +
                COL_CONFIDENCE + " REAL NOT NULL, " +
                COL_RULE_ID + " TEXT NOT NULL);"
        db.execSQL(createQuery)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_timestamp ON " + TABLE_BLOCK_EVENTS + " (" + COL_TIMESTAMP + ");")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_category ON " + TABLE_BLOCK_EVENTS + " (" + COL_CATEGORY + ");")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOCK_EVENTS)
        onCreate(db)
    }

    suspend fun insertBlockEvent(event: BlockEvent): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(COL_TIMESTAMP, event.timestamp)
            put(COL_PACKAGE_NAME, event.packageName)
            put(COL_CATEGORY, event.category.name)
            put(COL_CONFIDENCE, event.confidence)
            put(COL_RULE_ID, event.ruleId)
        }
        writableDatabase.insert(TABLE_BLOCK_EVENTS, null, values)
    }

    suspend fun getRecentEvents(limit: Int = 100): List<BlockEvent> = withContext(Dispatchers.IO) {
        val list = mutableListOf<BlockEvent>()
        val cursor = readableDatabase.query(
            TABLE_BLOCK_EVENTS,
            null,
            null,
            null,
            null,
            null,
            COL_TIMESTAMP + " DESC",
            limit.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(COL_ID))
                val ts = it.getLong(it.getColumnIndexOrThrow(COL_TIMESTAMP))
                val pkg = it.getString(it.getColumnIndexOrThrow(COL_PACKAGE_NAME))
                val catStr = it.getString(it.getColumnIndexOrThrow(COL_CATEGORY))
                val conf = it.getFloat(it.getColumnIndexOrThrow(COL_CONFIDENCE))
                val rule = it.getString(it.getColumnIndexOrThrow(COL_RULE_ID))
                val cat = runCatching { ContentCategory.valueOf(catStr) }.getOrDefault(ContentCategory.OTHER_SHORT_VIDEO)

                list.add(BlockEvent(id, ts, pkg, cat, conf, rule))
            }
        }
        list
    }

    suspend fun getCountSince(timestamp: Long): Int = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM " + TABLE_BLOCK_EVENTS + " WHERE " + COL_TIMESTAMP + " >= ?",
            arrayOf(timestamp.toString())
        )
        cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    suspend fun getCategoryCountSince(category: ContentCategory, timestamp: Long): Int = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM " + TABLE_BLOCK_EVENTS + " WHERE " + COL_CATEGORY + " = ? AND " + COL_TIMESTAMP + " >= ?",
            arrayOf(category.name, timestamp.toString())
        )
        cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    suspend fun getDailyStats(days: Int = 7): List<DailyStat> = withContext(Dispatchers.IO) {
        val result = mutableListOf<DailyStat>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        for (i in 0 until days) {
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, -2) // shift back for next loop iteration

            val cursor = readableDatabase.rawQuery(
                "SELECT " + COL_CATEGORY + ", COUNT(*) FROM " + TABLE_BLOCK_EVENTS +
                        " WHERE " + COL_TIMESTAMP + " >= ? AND " + COL_TIMESTAMP + " < ? GROUP BY " + COL_CATEGORY,
                arrayOf(dayStart.toString(), dayEnd.toString())
            )

            var total = 0
            var shorts = 0
            var reels = 0
            var spotlight = 0
            var adult = 0

            cursor.use {
                while (it.moveToNext()) {
                    val cat = it.getString(0)
                    val count = it.getInt(1)
                    total += count
                    when (cat) {
                        ContentCategory.YOUTUBE_SHORTS.name -> shorts += count
                        ContentCategory.INSTAGRAM_REELS.name,
                        ContentCategory.FACEBOOK_REELS.name,
                        ContentCategory.TIKTOK.name,
                        ContentCategory.OTHER_SHORT_VIDEO.name -> reels += count
                        ContentCategory.SNAPCHAT_SPOTLIGHT.name -> spotlight += count
                        ContentCategory.ADULT_WEBSITE.name, ContentCategory.ADULT_KEYWORD.name -> adult += count
                    }
                }
            }

            val minutesSaved = total * 3
            result.add(DailyStat(dayStart, total, shorts, reels, spotlight, adult, minutesSaved))
        }

        result.reversed()
    }

    suspend fun calculateFocusStreak(): Int = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        var streak = 0
        for (i in 0 until 365) {
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, -2)

            val count = getCountBetween(dayStart, dayEnd)
            if (count > 0) {
                streak++
            } else if (i > 0) {
                break
            }
        }
        streak
    }

    private fun getCountBetween(start: Long, end: Long): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM " + TABLE_BLOCK_EVENTS + " WHERE " + COL_TIMESTAMP + " >= ? AND " + COL_TIMESTAMP + " < ?",
            arrayOf(start.toString(), end.toString())
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        writableDatabase.delete(TABLE_BLOCK_EVENTS, null, null)
    }

    suspend fun exportCsv(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.append("id,timestamp,date_time,package_name,category,confidence,rule_id\n")
        val cursor = readableDatabase.query(TABLE_BLOCK_EVENTS, null, null, null, null, null, COL_TIMESTAMP + " ASC")
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(COL_ID))
                val ts = it.getLong(it.getColumnIndexOrThrow(COL_TIMESTAMP))
                val dt = DateTimeUtils.formatTimestamp(ts)
                val pkg = it.getString(it.getColumnIndexOrThrow(COL_PACKAGE_NAME))
                val cat = it.getString(it.getColumnIndexOrThrow(COL_CATEGORY))
                val conf = it.getFloat(it.getColumnIndexOrThrow(COL_CONFIDENCE))
                val rule = it.getString(it.getColumnIndexOrThrow(COL_RULE_ID))
                sb.append(id).append(",").append(ts).append(",\"").append(dt).append("\",")
                    .append(pkg).append(",").append(cat).append(",").append(conf).append(",")
                    .append(rule).append("\n")
            }
        }
        sb.toString()
    }
}
