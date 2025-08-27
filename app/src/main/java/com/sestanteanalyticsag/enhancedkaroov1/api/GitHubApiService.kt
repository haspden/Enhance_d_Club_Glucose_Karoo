package com.sestanteanalyticsag.enhancedkaroov1.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val body: String,
    val assets: List<GitHubAsset>
)

data class GitHubAsset(
    val name: String,
    val browser_download_url: String,
    val size: Long
)

interface GitHubApiService {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @retrofit2.http.Path("owner") owner: String,
        @retrofit2.http.Path("repo") repo: String
    ): GitHubRelease
    
    @GET
    suspend fun downloadApk(@Url url: String): okhttp3.ResponseBody
}
