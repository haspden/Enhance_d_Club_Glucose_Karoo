package com.sestanteanalyticsag.enhancedkaroov1.repository

import com.sestanteanalyticsag.enhancedkaroov1.api.GlucoseApiService
import com.sestanteanalyticsag.enhancedkaroov1.data.GlucoseEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class GlucoseRepository {
    private var apiService: GlucoseApiService? = null
    private var currentBaseUrl: String = ""
    
    private fun createApiService(fullUrl: String): GlucoseApiService {
        // Extract base URL from full URL
        val baseUrl = extractBaseUrl(fullUrl)
        println("GlucoseRepository: Creating API service with base URL: $baseUrl")
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GlucoseApiService::class.java)
    }
    
    private fun extractBaseUrl(fullUrl: String): String {
        return try {
            val url = java.net.URL(fullUrl)
            val baseUrl = "${url.protocol}://${url.host}"
            if (url.port != -1) {
                "$baseUrl:${url.port}"
            } else {
                baseUrl
            } + "/"
        } catch (e: Exception) {
            println("GlucoseRepository: Error extracting base URL from $fullUrl: ${e.message}")
            // Fallback to default
            "https://haspdenbloodglucose.herokuapp.com/"
        }
    }
    
    fun updateBaseUrl(fullUrl: String) {
        if (currentBaseUrl != fullUrl) {
            currentBaseUrl = fullUrl
            apiService = createApiService(fullUrl)
            println("GlucoseRepository: Updated full URL to: $fullUrl")
        }
    }
    
    private var cachedGlucoseEntry: GlucoseEntry? = null
    private var lastFetchTime: Long = 0
    
               suspend fun getLatestGlucose(): Result<GlucoseEntry> = withContext(Dispatchers.IO) {
               try {
                   val service = apiService ?: throw IOException("API service not initialized. Please set a base URL.")
                   println("GlucoseRepository: Fetching from API...")
                   val entries = service.getGlucoseEntries()
                   println("GlucoseRepository: Received ${entries.size} entries")

                   if (entries.isNotEmpty()) {
                       val latestEntry = entries.first()
                       println("GlucoseRepository: Latest entry - SGV: ${latestEntry.sgv}, Direction: ${latestEntry.direction}")
                       cachedGlucoseEntry = latestEntry
                       lastFetchTime = System.currentTimeMillis()
                       Result.success(latestEntry)
                   } else {
                       println("GlucoseRepository: No entries found")
                       Result.failure(IOException("No glucose data available"))
                   }
               } catch (e: Exception) {
                   println("GlucoseRepository: Exception occurred: ${e.message}")
                   e.printStackTrace()
                   Result.failure(e)
               }
           }
           
           suspend fun getGlucoseEntries(): Result<List<GlucoseEntry>> = withContext(Dispatchers.IO) {
               try {
                   val service = apiService ?: throw IOException("API service not initialized. Please set a base URL.")
                   println("GlucoseRepository: Fetching entries from API...")
                   val entries = service.getGlucoseEntries()
                   println("GlucoseRepository: Received ${entries.size} entries")
                   Result.success(entries)
               } catch (e: Exception) {
                   println("GlucoseRepository: Exception occurred: ${e.message}")
                   e.printStackTrace()
                   Result.failure(e)
               }
           }
    
    fun getCachedGlucose(): GlucoseEntry? = cachedGlucoseEntry
    
    fun getLastFetchTime(): Long = lastFetchTime
    
    fun shouldRefresh(): Boolean {
        val currentTime = System.currentTimeMillis()
        val fifteenSeconds = 15 * 1000L
        return (currentTime - lastFetchTime) >= fifteenSeconds
    }
}
