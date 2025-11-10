package com.sestanteanalyticsag.enhancedkaroov1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sestanteanalyticsag.enhancedkaroov1.databinding.ActivityMainBinding
import com.sestanteanalyticsag.enhancedkaroov1.repository.GlucoseRepository
import com.sestanteanalyticsag.enhancedkaroov1.util.UpdateChecker
import com.sestanteanalyticsag.enhancedkaroov1.util.UpdateResult
import com.sestanteanalyticsag.enhancedkaroov1.util.DownloadResult
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.RideState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val karooSystem by lazy { KarooSystemService(this) }
    private val glucoseRepository = GlucoseRepository()
    private var consumerId: String? = null
    
    companion object {
        private const val TAG = "MainActivity"
        private const val REFRESH_INTERVAL = 15000L // 15 seconds
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Show medical disclaimer if not previously accepted
        if (!isDisclaimerAccepted()) {
            showMedicalDisclaimer()
        } else {
            initializeApp()
        }
    }
    
    private fun initializeApp() {
        setupUI()
        setupKarooConnection()
        startGlucoseMonitoring()
        
        // Check for updates after a short delay to let the app fully load
        lifecycleScope.launch {
            delay(2000) // 2 second delay
            checkForUpdatesOnStartup()
        }
    }
    
    private fun showMedicalDisclaimer() {
        val disclaimerText = """
            ⚠️ MEDICAL DISCLAIMER ⚠️
            
            Enhance-d Club Glucose - Karoo is an OPEN SOURCE application intended for informational and educational purposes ONLY.
            
            This application is NOT intended for:
            • Medical diagnosis, treatment, or decision-making
            • Replacing professional medical advice
            • Emergency medical situations
            • Life-critical diabetes management decisions
            
            IMPORTANT WARNINGS:
            • Always consult qualified healthcare professionals for medical decisions
            • Do not rely on this app for diabetes management
            • This app may not be accurate, complete, or reliable
            • Open source software comes with NO WARRANTIES
            • Use entirely at your own risk
            
            OPEN SOURCE NOTICE:
            • This software is provided "AS IS" without warranty of any kind
            • The developers and contributors are not medical professionals
            • Code is publicly available and may be modified by anyone
            • No regulatory approval has been sought or obtained
            • You assume all risks associated with using open source medical software
            
            By accepting, you acknowledge that:
            • You understand this is experimental open source software
            • You will not use this app for medical treatment decisions
            • You accept full responsibility for any consequences of use
            • You will consult healthcare professionals for all medical advice
            • You understand the limitations of open source medical tools
            
            If you do not agree to these terms, please decline and discontinue use.
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Medical Disclaimer")
            .setMessage(disclaimerText)
            .setCancelable(false)
            .setPositiveButton("I Accept") { _, _ ->
                saveDisclaimerAcceptance()
                initializeApp()
            }
            .setNegativeButton("I Decline") { _, _ ->
                finish() // Close the app if user declines
            }
            .show()
    }
    
    private fun isDisclaimerAccepted(): Boolean {
        val prefs = getSharedPreferences("GlucoseDataFieldPrefs", MODE_PRIVATE)
        return prefs.getBoolean("disclaimer_accepted", false)
    }
    
    private fun saveDisclaimerAcceptance() {
        val prefs = getSharedPreferences("GlucoseDataFieldPrefs", MODE_PRIVATE)
        prefs.edit().putBoolean("disclaimer_accepted", true).apply()
    }
    
    private fun setupUI() {
        // Set up settings button
        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        
        // Initialize repository with stored URL and API token
        val currentUrl = getStoredUrl()
        val currentApiToken = getStoredApiToken()
        glucoseRepository.updateBaseUrl(currentUrl)
        glucoseRepository.updateApiToken(currentApiToken)
    }
    
    private fun setupKarooConnection() {
        karooSystem.connect {
            Log.d(TAG, "Karoo system connected")
            runOnUiThread {
                Toast.makeText(this, "Connected to Karoo", Toast.LENGTH_SHORT).show()
            }
            
            // Subscribe to ride state changes
            consumerId = karooSystem.addConsumer { rideState: RideState ->
                Log.d(TAG, "Ride state changed to: $rideState")
                runOnUiThread {
                    binding.tvRideState.text = "Ride State: $rideState"
                }
            }
            
            // Try to start the extension service
            startExtensionService()
        }
    }
    
    private fun startExtensionService() {
        try {
            Log.d(TAG, "Attempting to start extension service...")
            val intent = Intent("io.hammerhead.karooext.KAROO_EXTENSION")
            intent.setPackage(packageName)
            intent.setClassName(packageName, "$packageName.GlucoseExtension")
            startService(intent)
            Log.d(TAG, "Extension service start intent sent")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting extension service", e)
        }
    }
    
    private fun startGlucoseMonitoring() {
        lifecycleScope.launch {
            while (true) {
                fetchGlucoseData()
                delay(REFRESH_INTERVAL)
            }
        }
    }
    
    private suspend fun fetchGlucoseData() {
        try {
            Log.d(TAG, "Fetching glucose data...")
            val result = glucoseRepository.getLatestGlucose()
            result.fold(
                onSuccess = { glucoseEntry ->
                    Log.d(TAG, "Glucose: ${glucoseEntry.getGlucoseValue()} mg/dl, Direction: ${glucoseEntry.getDirectionArrow()}")
        Log.d(TAG, "Raw glucose value: ${glucoseEntry.getGlucoseValue()} mg/dl, converted to: ${glucoseEntry.getGlucoseValue() / 18.0} mmol/L")
        Log.d(TAG, "Data timestamp: ${glucoseEntry.date}")
                    runOnUiThread {
                        updateGlucoseDisplay(glucoseEntry)
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error fetching glucose data", exception)
                    runOnUiThread {
                        binding.tvGlucoseValue.text = "Error"
                        binding.tvGlucoseUnit.text = ""
                        binding.tvGlucoseMmol.text = ""
                        binding.tvDirection.text = "Error: ${exception.message}"
                        binding.tvLastUpdated.text = "Failed to fetch data"
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception in fetchGlucoseData", e)
            runOnUiThread {
                binding.tvGlucoseValue.text = "Exception"
                binding.tvGlucoseUnit.text = ""
                binding.tvGlucoseMmol.text = ""
                binding.tvDirection.text = "Exception: ${e.message}"
                binding.tvLastUpdated.text = "Exception occurred"
            }
        }
    }
    
    private fun updateGlucoseDisplay(glucoseEntry: com.sestanteanalyticsag.enhancedkaroov1.data.GlucoseEntry) {
        val glucoseValueMgdl = glucoseEntry.getGlucoseValue()
        val glucoseValueMmol = glucoseValueMgdl / 18.0
        val direction = glucoseEntry.getDirectionArrow()
        
        // Display mg/dl
        binding.tvGlucoseValue.text = glucoseValueMgdl.toString()
        binding.tvGlucoseUnit.text = "mg/dl"
        
        // Display mmol/L with 1 decimal place
        binding.tvGlucoseMmol.text = String.format("%.1f mmol/L", glucoseValueMmol)
        
        binding.tvDirection.text = direction
        
        // Color coding based on glucose levels
        val color = when {
            glucoseValueMgdl < 70 -> getColor(R.color.glucose_low)
            glucoseValueMgdl > 180 -> getColor(R.color.glucose_high)
            else -> getColor(R.color.glucose_normal)
        }
        
        binding.tvGlucoseValue.setTextColor(color)
        binding.tvGlucoseMmol.setTextColor(color)
        
        // Format last updated time
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val lastUpdated = dateFormat.format(Date(glucoseEntry.date))
        binding.tvLastUpdated.text = "Last updated: $lastUpdated"
    }
    
    private fun getStoredUrl(): String {
        val prefs = getSharedPreferences("GlucoseDataFieldPrefs", MODE_PRIVATE)
        return prefs.getString("nightscout_url", "http://127.0.0.1:17580/sgv.json") ?: "http://127.0.0.1:17580/sgv.json"
    }
    
    private fun getStoredApiToken(): String {
        val prefs = getSharedPreferences("GlucoseDataFieldPrefs", MODE_PRIVATE)
        return prefs.getString("api_token", "") ?: ""
    }
    

    
    private fun checkForUpdatesOnStartup() {
        // Only check for updates once per app session to avoid annoying users
        if (isUpdateCheckAlreadyDone()) {
            return
        }
        
        val updateChecker = UpdateChecker(this)
        
        lifecycleScope.launch {
            try {
                val result = updateChecker.checkForUpdates()
                
                when (result) {
                    is UpdateResult.UpdateAvailable -> {
                        showUpdateAvailableDialog(result, updateChecker)
                    }
                    is UpdateResult.UpToDate -> {
                        // Silently log that we're up to date
                        Log.d(TAG, "App is up to date (${result.version})")
                    }
                    is UpdateResult.Error -> {
                        // Silently log errors to avoid annoying users
                        Log.w(TAG, "Update check failed: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception during update check", e)
            }
        }
    }
    
    private fun showUpdateAvailableDialog(updateResult: UpdateResult.UpdateAvailable, updateChecker: UpdateChecker) {
        val fileSizeMB = updateResult.fileSize / (1024 * 1024)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("🔄 Update Available")
            .setMessage("""
                A new version of Enhance-d Club Glucose is available!
                
                Current: ${updateResult.currentVersion}
                New: ${updateResult.newVersion}
                Size: ${fileSizeMB}MB
                
                ${updateResult.releaseNotes.take(150)}${if (updateResult.releaseNotes.length > 150) "..." else ""}
                
                Would you like to update now?
            """.trimIndent())
            .setPositiveButton("Update Now") { _, _ ->
                downloadAndInstallUpdate(updateResult, updateChecker)
            }
            .setNegativeButton("Later") { _, _ ->
                // Mark that we've shown the update dialog for this session
                markUpdateCheckDone()
            }
            .setNeutralButton("View Full Notes") { _, _ ->
                showFullReleaseNotes(updateResult.releaseNotes)
            }
            .setCancelable(false) // Prevent dismissing by tapping outside
            .create()
        
        dialog.show()
    }
    
    private fun downloadAndInstallUpdate(updateResult: UpdateResult.UpdateAvailable, updateChecker: UpdateChecker) {
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("Downloading Update")
            .setMessage("Downloading version ${updateResult.newVersion}...")
            .setCancelable(false)
            .create()
        progressDialog.show()
        
        lifecycleScope.launch {
            try {
                val downloadResult = updateChecker.downloadUpdate(updateResult.downloadUrl)
                
                progressDialog.dismiss()
                
                when (downloadResult) {
                    is DownloadResult.Success -> {
                        try {
                            updateChecker.installUpdate(downloadResult.apkFile)
                            Toast.makeText(this@MainActivity, "Installation started", Toast.LENGTH_SHORT).show()
                            // Mark update check as done since we're installing
                            markUpdateCheckDone()
                        } catch (e: Exception) {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Installation Failed")
                                .setMessage("Error: ${e.message}")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                    is DownloadResult.Error -> {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Download Failed")
                            .setMessage("Error: ${downloadResult.message}")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Download Failed")
                    .setMessage("Exception: ${e.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
    
    private fun showFullReleaseNotes(releaseNotes: String) {
        AlertDialog.Builder(this)
            .setTitle("Release Notes")
            .setMessage(releaseNotes)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun isUpdateCheckAlreadyDone(): Boolean {
        val prefs = getSharedPreferences("GlucoseDataFieldPrefs", MODE_PRIVATE)
        return prefs.getBoolean("update_check_done_${Constants.APP_VERSION}", false)
    }
    
    private fun markUpdateCheckDone() {
        val prefs = getSharedPreferences("GlucoseDataFieldPrefs", MODE_PRIVATE)
        prefs.edit().putBoolean("update_check_done_${Constants.APP_VERSION}", true).apply()
    }
    
    override fun onDestroy() {
        consumerId?.let { karooSystem.removeConsumer(it) }
        karooSystem.disconnect()
        super.onDestroy()
    }
}
