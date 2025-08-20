package com.sestanteanalyticsag.enhancedkaroov1.api

import com.sestanteanalyticsag.enhancedkaroov1.data.GlucoseEntry
import retrofit2.http.GET

interface GlucoseApiService {
    @GET("api/v1/entries/sgv.json")
    suspend fun getGlucoseEntries(): List<GlucoseEntry>
}
