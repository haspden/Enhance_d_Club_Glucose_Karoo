package com.sestanteanalyticsag.enhancedkaroov1.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.sestanteanalyticsag.enhancedkaroov1.Constants
import com.sestanteanalyticsag.enhancedkaroov1.api.GitHubApiService
import com.sestanteanalyticsag.enhancedkaroov1.api.GitHubRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class UpdateChecker(private val context: Context) {
    
    companion object {
        private const val TAG = "UpdateChecker"
        private const val GITHUB_API_BASE_URL = "https://api.github.com/"
        private const val DOWNLOAD_DIR = "updates"
        
        // Repository details
        private const val REPO_OWNER = "haspden"
        private const val REPO_NAME = "Enhance_d_Club_Glucose_Karoo"
    }
    
    private val githubApiService: GitHubApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        
        Retrofit.Builder()
            .baseUrl(GITHUB_API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubApiService::class.java)
    }
    
    suspend fun checkForUpdates(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Checking for updates...")
            val latestRelease = githubApiService.getLatestRelease(REPO_OWNER, REPO_NAME)
            
            val currentVersion = Constants.APP_VERSION
            val latestVersion = latestRelease.tag_name.removePrefix("v")
            
            Log.d(TAG, "Current version: $currentVersion, Latest version: $latestVersion")
            
            if (isNewerVersion(currentVersion, latestVersion)) {
                val apkAsset = latestRelease.assets.find { it.name.endsWith(".apk") }
                if (apkAsset != null) {
                    UpdateResult.UpdateAvailable(
                        currentVersion = currentVersion,
                        newVersion = latestVersion,
                        releaseNotes = latestRelease.body,
                        downloadUrl = apkAsset.browser_download_url,
                        fileSize = apkAsset.size
                    )
                } else {
                    UpdateResult.Error("No APK found in latest release")
                }
            } else {
                UpdateResult.UpToDate(currentVersion)
            }
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP Error checking for updates: ${e.code()}", e)
            when (e.code()) {
                404 -> UpdateResult.Error("No releases found. Please check if the repository has published releases on GitHub.")
                403 -> UpdateResult.Error("Rate limit exceeded. Please try again later.")
                else -> UpdateResult.Error("GitHub API error (${e.code()}): ${e.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            UpdateResult.Error("Failed to check for updates: ${e.message}")
        }
    }
    
    suspend fun downloadUpdate(downloadUrl: String): DownloadResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Downloading update from: $downloadUrl")
            
            // Create download directory
            val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), DOWNLOAD_DIR)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            
            // Create temporary file
            val apkFile = File(downloadDir, "Enhance-d_Club_Glucose_Karoo_update.apk")
            
            // Download the APK
            val response = githubApiService.downloadApk(downloadUrl)
            
            // Write to file
            val inputStream = response.byteStream()
            val outputStream = FileOutputStream(apkFile)
            
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytes = 0L
            val totalSize = response.contentLength()
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytes += bytesRead
                
                // Calculate progress
                val progress = if (totalSize > 0) {
                    (totalBytes * 100 / totalSize).toInt()
                } else {
                    0
                }
                
                Log.d(TAG, "Download progress: $progress% ($totalBytes/$totalSize bytes)")
            }
            
            inputStream.close()
            outputStream.close()
            
            Log.d(TAG, "Download completed: ${apkFile.absolutePath}")
            DownloadResult.Success(apkFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading update", e)
            DownloadResult.Error("Download failed: ${e.message}")
        }
    }
    
    fun installUpdate(apkFile: File) {
        try {
            Log.d(TAG, "Installing update: ${apkFile.absolutePath}")
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error installing update", e)
            throw e
        }
    }
    
    private fun isNewerVersion(currentVersion: String, newVersion: String): Boolean {
        val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val newParts = newVersion.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(currentParts.size, newParts.size)
        
        for (i in 0 until maxLength) {
            val current = currentParts.getOrNull(i) ?: 0
            val new = newParts.getOrNull(i) ?: 0
            
            if (new > current) return true
            if (new < current) return false
        }
        
        return false // Versions are equal
    }
}

sealed class UpdateResult {
    data class UpdateAvailable(
        val currentVersion: String,
        val newVersion: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val fileSize: Long
    ) : UpdateResult()
    
    data class UpToDate(val version: String) : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

sealed class DownloadResult {
    data class Success(val apkFile: File) : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}
