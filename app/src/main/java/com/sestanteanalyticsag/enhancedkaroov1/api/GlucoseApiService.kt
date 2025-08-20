package com.sestanteanalyticsag.enhancedkaroov1.api

import com.sestanteanalyticsag.enhancedkaroov1.data.GlucoseEntry
import retrofit2.http.GET
import retrofit2.http.Header

interface GlucoseApiService {
    @GET("api/v1/entries/sgv.json")
    suspend fun getGlucoseEntries(
        @Header("api-secret") apiSecret: String? = null,
        @Header("Authorization") authorization: String? = null
    ): List<GlucoseEntry>
}
