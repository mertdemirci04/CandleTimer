package com.example.candletime.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Entity(tableName = "candle_sessions")
data class CandleSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startedAt: Long,
    val plannedSeconds: Long,
    val actualSeconds: Long,
    val completed: Boolean
) {

    val completionPercent: Int
        get() = if (plannedSeconds > 0) {
            ((actualSeconds.toFloat() / plannedSeconds.toFloat()) * 100)
                .coerceIn(0f, 100f)
                .toInt()
        } else 0


    fun formattedDayOfWeek(): String {
        return Instant.ofEpochMilli(startedAt)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("EEEE", Locale("tr")))
    }


    fun formattedDate(): String {
        return Instant.ofEpochMilli(startedAt)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr")))
    }


    fun formattedTime(): String {
        return Instant.ofEpochMilli(startedAt)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    fun durationLabel(): String {
        val h = plannedSeconds / 3600
        val m = (plannedSeconds % 3600) / 60
        return when {
            h > 0 && m > 0 -> "${h}s ${m}dk"
            h > 0 -> "${h}s"
            else -> "${m}dk"
        }
    }

    fun actualDurationLabel(): String {
        val h = actualSeconds / 3600
        val m = (actualSeconds % 3600) / 60
        return when {
            h > 0 && m > 0 -> "${h}s ${m}dk"
            h > 0 -> "${h}s"
            else -> "${m}dk"
        }
    }
}