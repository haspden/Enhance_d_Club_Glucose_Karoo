package com.sestanteanalyticsag.enhancedkaroov1

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sestanteanalyticsag.enhancedkaroov1.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    
    companion object {
        private const val TAG = "SettingsActivity"
        private const val PREFS_NAME = "GlucoseDataFieldPrefs"
        private const val KEY_NIGHTSCOUT_URL = "nightscout_url"
        private const val KEY_API_TOKEN = "api_token"
        private const val KEY_AUTO_REFRESH = "auto_refresh"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val DEFAULT_URL = "http://127.0.0.1:17580/sgv.json"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    private fun setupUI() {
        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        
        // Display current URL
        val currentUrl = getStoredUrl()
        binding.tvCurrentUrl.text = currentUrl
        
        // Display current API token status
        val currentToken = getStoredApiToken()
        binding.tvCurrentApiToken.text = if (currentToken.isNotEmpty()) "Configured: $currentToken" else "Not configured"
        
        // Set up URL configuration button
        binding.btnConfigureUrl.setOnClickListener {
            showUrlConfigurationDialog()
        }
        
        // Set up API token configuration button
        binding.btnConfigureApiToken.setOnClickListener {
            showApiTokenConfigurationDialog()
        }
        
        // Set up restart app button
        binding.btnRestartApp.setOnClickListener {
            restartApp()
        }
        
        // Set up view disclaimer button
        binding.btnViewDisclaimer.setOnClickListener {
            showMedicalDisclaimer()
        }
        
        // Set up other settings with persistence
        binding.switchAutoRefresh.isChecked = getStoredAutoRefresh()
        binding.switchNotifications.isChecked = getStoredNotifications()
        
        // Set up switch listeners for persistence
        binding.switchAutoRefresh.setOnCheckedChangeListener { _, isChecked ->
            saveAutoRefresh(isChecked)
        }
        
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveNotifications(isChecked)
        }
    }
    
    private fun showUrlConfigurationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_url_config, null)
        val urlEditText = dialogView.findViewById<android.widget.EditText>(R.id.etNightscoutUrl)
        
        // Set current URL
        urlEditText.setText(getStoredUrl())
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newUrl = urlEditText.text.toString().trim()
                if (newUrl.isNotEmpty()) {
                    saveUrl(newUrl)
                    binding.tvCurrentUrl.text = newUrl
                    Toast.makeText(this, "URL updated. Restart app to apply changes.", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        // Set up quick fill buttons
        dialogView.findViewById<android.widget.Button>(R.id.btnLocalWeb).setOnClickListener {
            urlEditText.setText("http://127.0.0.1:17580/sgv.json")
        }
        
        dialogView.findViewById<android.widget.Button>(R.id.btnHotspot).setOnClickListener {
            val gatewayIp = getDefaultGatewayIp()
            if (gatewayIp != null) {
                urlEditText.setText("http://$gatewayIp:17580/sgv.json")
            } else {
                Toast.makeText(this, "Could not detect gateway IP", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialogView.findViewById<android.widget.Button>(R.id.btnNightscout).setOnClickListener {
            urlEditText.setText("https://mynightscout.com/api/v1/entries/sgv.json")
        }
        

        
        dialog.show()
    }
    
    private fun showApiTokenConfigurationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_api_token_config, null)
        val tokenEditText = dialogView.findViewById<android.widget.EditText>(R.id.etApiToken)
        val showTokenSwitch = dialogView.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchShowToken)
        
        // Set current token
        val currentToken = getStoredApiToken()
        if (currentToken.isNotEmpty()) {
            tokenEditText.setText(currentToken)
        }
        
        // Set up show/hide toggle
        showTokenSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tokenEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                tokenEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newToken = tokenEditText.text.toString().trim()
                if (newToken.isNotEmpty()) {
                    saveApiToken(newToken)
                    binding.tvCurrentApiToken.text = "Configured: $newToken"
                    Toast.makeText(this, "API token updated. Restart app to apply changes.", Toast.LENGTH_LONG).show()
                } else {
                    // Clear the token
                    saveApiToken("")
                    binding.tvCurrentApiToken.text = "Not configured"
                    Toast.makeText(this, "API token cleared. Restart app to apply changes.", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Clear Token") { _, _ ->
                saveApiToken("")
                binding.tvCurrentApiToken.text = "Not configured"
                Toast.makeText(this, "API token cleared. Restart app to apply changes.", Toast.LENGTH_LONG).show()
            }
            .create()
        
        dialog.show()
    }
    
    private fun getDefaultGatewayIp(): String? {
        return try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            
            // Method 1: Calculate gateway from device IP (most common)
            if (ipAddress != 0) {
                val gatewayIp = (ipAddress and 0xFF).toString() + "." +
                        ((ipAddress shr 8) and 0xFF) + "." +
                        ((ipAddress shr 16) and 0xFF) + ".1"
                Log.d(TAG, "Detected gateway IP: $gatewayIp from device IP: $ipAddress")
                return gatewayIp
            }
            
            // Method 2: Try common gateway IPs
            val commonGateways = listOf("192.168.1.1", "192.168.0.1", "10.0.0.1", "172.16.0.1")
            for (gateway in commonGateways) {
                try {
                    val runtime = Runtime.getRuntime()
                    val process = runtime.exec("ping -c 1 -W 1000 $gateway")
                    val exitCode = process.waitFor()
                    if (exitCode == 0) {
                        Log.d(TAG, "Found working gateway: $gateway")
                        return gateway
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Gateway $gateway not reachable: ${e.message}")
                }
            }
            
            // No gateway found
            Log.d(TAG, "No gateway IP detected")
            return null
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting gateway IP", e)
            return null
        }
    }
    
    private fun getStoredUrl(): String {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NIGHTSCOUT_URL, DEFAULT_URL) ?: DEFAULT_URL
    }
    
    private fun saveUrl(url: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NIGHTSCOUT_URL, url).apply()
    }
    
    private fun getStoredApiToken(): String {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_TOKEN, "") ?: ""
    }
    
    private fun saveApiToken(token: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_API_TOKEN, token).apply()
    }
    
    private fun getStoredAutoRefresh(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_REFRESH, true) // Default to true
    }
    
    private fun saveAutoRefresh(enabled: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_REFRESH, enabled).apply()
    }
    
    private fun getStoredNotifications(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS, true) // Default to true
    }
    
    private fun saveNotifications(enabled: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }
    
    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
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
            
            By using this app, you acknowledge that:
            • You understand this is experimental open source software
            • You will not use this app for medical treatment decisions
            • You accept full responsibility for any consequences of use
            • You will consult healthcare professionals for all medical advice
            • You understand the limitations of open source medical tools
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Medical Disclaimer")
            .setMessage(disclaimerText)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
