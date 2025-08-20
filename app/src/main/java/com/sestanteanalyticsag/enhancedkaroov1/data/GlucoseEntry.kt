package com.sestanteanalyticsag.enhancedkaroov1.data

import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit

data class GlucoseEntry(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("app")
    val app: String,
    
    @SerializedName("date")
    val date: Long,
    
    @SerializedName("device")
    val device: String,
    
    @SerializedName("direction")
    val direction: String,
    
    @SerializedName("isReadOnly")
    val isReadOnly: Boolean,
    
    @SerializedName("isValid")
    val isValid: Boolean,
    
    @SerializedName("sgv")
    val sgv: Int,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("unfiltered")
    val unfiltered: Int,
    
    @SerializedName("units")
    val units: String,
    
    @SerializedName("utcOffset")
    val utcOffset: Int,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("identifier")
    val identifier: String,
    
    @SerializedName("srvModified")
    val srvModified: Long,
    
    @SerializedName("srvCreated")
    val srvCreated: Long,
    
    @SerializedName("subject")
    val subject: String,
    
    @SerializedName("mills")
    val mills: Long
) {
    fun getGlucoseValue(): Int = sgv
    
    fun getDirectionArrow(): String {
        return when (direction) {
            "DoubleUp" -> "↑↑"
            "SingleUp" -> "↑"
            "FortyFiveUp" -> "↗"
            "Flat" -> "→"
            "FortyFiveDown" -> "↘"
            "SingleDown" -> "↓"
            "DoubleDown" -> "↓↓"
            else -> "?"
        }
    }
    
    fun isRecent(): Boolean {
        val currentTime = System.currentTimeMillis()
        val tenMinutes = TimeUnit.MINUTES.toMillis(10)
        return (currentTime - date) <= tenMinutes
    }
    
    fun getTimeSinceSeconds(): Long {
        val currentTime = System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toSeconds(currentTime - date)
    }
    
    fun getTimeSinceFormatted(): String {
        val seconds = getTimeSinceSeconds()
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> {
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                "${minutes}:${String.format("%02d", remainingSeconds)}"
            }
            else -> {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val remainingSeconds = seconds % 60
                "${hours}:${String.format("%02d", minutes)}:${String.format("%02d", remainingSeconds)}"
            }
        }
    }
    
    fun getTimeSinceFormattedNumeric(): Double {
        val seconds = getTimeSinceSeconds()
        return when {
            seconds < 60 -> seconds.toDouble()
            seconds < 3600 -> {
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                // Return as minutes.seconds (e.g., 2.30 for 2:30)
                minutes + (remainingSeconds / 100.0)
            }
            else -> {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val remainingSeconds = seconds % 60
                // Return as hours.minutes.seconds (e.g., 1.23.45 for 1:23:45)
                hours + (minutes / 100.0) + (remainingSeconds / 10000.0)
            }
        }
    }
}
