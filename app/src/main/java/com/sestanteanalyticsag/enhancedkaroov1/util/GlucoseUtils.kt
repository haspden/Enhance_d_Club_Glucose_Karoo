package com.sestanteanalyticsag.enhancedkaroov1.util

import com.sestanteanalyticsag.enhancedkaroov1.data.GlucoseEntry
import java.util.concurrent.TimeUnit

object GlucoseUtils {
    
    fun calculateDelta(entries: List<GlucoseEntry>, minutes: Int): String {
        if (entries.size < 2) return "--"
        
        val currentTime = System.currentTimeMillis()
        val targetTime = currentTime - TimeUnit.MINUTES.toMillis(minutes.toLong())
        
        // Find the entry closest to the target time
        val targetEntry = entries.find { entry ->
            entry.date <= targetTime
        } ?: return "--"
        
        val latestEntry = entries.first()
        val delta = latestEntry.sgv - targetEntry.sgv
        
        return when {
            delta > 0 -> "+$delta"
            else -> "$delta"
        }
    }
    
    fun calculateDelta5m(entries: List<GlucoseEntry>): String {
        return calculateDelta(entries, 5)
    }
    
    fun calculateDelta15m(entries: List<GlucoseEntry>): String {
        return calculateDelta(entries, 15)
    }
    
    fun calculateDeltaMg(entries: List<GlucoseEntry>, minutes: Int): Double {
        if (entries.size < 2) return 0.0
        
        val currentTime = System.currentTimeMillis()
        val targetTime = currentTime - TimeUnit.MINUTES.toMillis(minutes.toLong())
        
        val targetEntry = entries.find { entry ->
            entry.date <= targetTime
        } ?: return 0.0
        
        val latestEntry = entries.first()
        return (latestEntry.sgv - targetEntry.sgv).toDouble()
    }
    
    fun calculateDeltaMmol(entries: List<GlucoseEntry>, minutes: Int): Double {
        val deltaMg = calculateDeltaMg(entries, minutes)
        val deltaMmol = deltaMg / 18.0182 // Convert mg/dl to mmol/L
        // Force decimal display by adding a tiny fractional part to whole numbers
        return if (deltaMmol == deltaMmol.toInt().toDouble()) {
            deltaMmol + 0.001 // Add tiny decimal to force display
        } else {
            deltaMmol
        }
    }
    
    fun calculateDelta5mMg(entries: List<GlucoseEntry>): Double {
        return calculateDeltaMg(entries, 5)
    }
    
    fun calculateDelta5mMmol(entries: List<GlucoseEntry>): Double {
        return calculateDeltaMmol(entries, 5)
    }
    
    fun calculateDelta15mMg(entries: List<GlucoseEntry>): Double {
        return calculateDeltaMg(entries, 15)
    }
    
    fun calculateDelta15mMmol(entries: List<GlucoseEntry>): Double {
        return calculateDeltaMmol(entries, 15)
    }
    
    fun isDataStale(entry: GlucoseEntry): Boolean {
        val currentTime = System.currentTimeMillis()
        val tenMinutes = TimeUnit.MINUTES.toMillis(10)
        return (currentTime - entry.date) > tenMinutes
    }
}
