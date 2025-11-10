package com.sestanteanalyticsag.enhancedkaroov1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ExtensionBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "ExtensionBroadcastReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                val packageName = intent.data?.schemeSpecificPart
                Log.d(TAG, "Package event: ${intent.action} for package: $packageName")
                
                if (packageName == context.packageName) {
                    Log.d(TAG, "Our app was installed/updated, notifying Karoo system")
                    notifyKarooSystem(context)
                }
            }
        }
    }
    
    private fun notifyKarooSystem(context: Context) {
        try {
            // Send a broadcast to notify the Karoo system about our extension
            val karooIntent = Intent("io.hammerhead.karooext.EXTENSION_AVAILABLE")
            karooIntent.putExtra("package_name", context.packageName)
            karooIntent.putExtra("extension_id", "enhanced_glucose")
            context.sendBroadcast(karooIntent)
            Log.d(TAG, "Sent Karoo extension notification")
        } catch (e: Exception) {
            Log.e(TAG, "Error notifying Karoo system", e)
        }
    }
}


