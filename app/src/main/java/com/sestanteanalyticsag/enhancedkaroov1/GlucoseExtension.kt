package com.sestanteanalyticsag.enhancedkaroov1

import android.content.Context
import com.sestanteanalyticsag.enhancedkaroov1.data.GlucoseEntry
import com.sestanteanalyticsag.enhancedkaroov1.repository.GlucoseRepository
import com.sestanteanalyticsag.enhancedkaroov1.util.GlucoseUtils
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.UpdateNumericConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.awaitCancellation
import java.text.SimpleDateFormat
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt

class GlucoseExtension : KarooExtension("enhanced_glucose", "1.0.0") {
    
    private val glucoseRepository = GlucoseRepository()
    private val extensionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onCreate() {
        super.onCreate()
        println("$TAG: Service created!")
        
        // Initialize repository with stored URL and API token
        try {
            val context = applicationContext
            val prefs = context.getSharedPreferences("GlucoseDataFieldPrefs", Context.MODE_PRIVATE)
            val storedUrl = prefs.getString("nightscout_url", "http://127.0.0.1:17580/sgv.json") ?: "http://127.0.0.1:17580/sgv.json"
            val storedApiToken = prefs.getString("api_token", "") ?: ""
            
            glucoseRepository.updateBaseUrl(storedUrl)
            glucoseRepository.updateApiToken(storedApiToken)
            println("$TAG: Repository initialized with URL: $storedUrl and API token: ${if (storedApiToken.isNotEmpty()) "***" else "none"}")
        } catch (e: Exception) {
            println("$TAG: Error initializing repository: ${e.message}")
        }
    }
    
    companion object {
        private const val TAG = "GlucoseExtension"
        private const val REFRESH_INTERVAL = 15000L // 15 seconds
    }
    
    override val types by lazy {
        listOf(
            GlucoseDataType(extensionScope, glucoseRepository),
            GlucoseMmolDataType(extensionScope, glucoseRepository),
            TimeSinceDataType(extensionScope, glucoseRepository),
            TimeSinceFormattedDataType(extensionScope, glucoseRepository),
            DirectionArrowDataType(extensionScope, glucoseRepository),
            Delta15mMgDataType(extensionScope, glucoseRepository),
            Delta15mMmolDataType(extensionScope, glucoseRepository),
            Delta5mMgDataType(extensionScope, glucoseRepository),
            Delta5mMmolDataType(extensionScope, glucoseRepository)
        )
    }
    

    
    override fun onDestroy() {
        extensionScope.cancel()
        super.onDestroy()
    }
}

class GlucoseDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "glucose_mg") {
    
    companion object {
        private const val TAG = "GlucoseDataType"
        private const val REFRESH_INTERVAL = 1000L // 1 second for UI updates
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
        println("$TAG: startStream called")
        
        // Immediately emit searching state
        emitter.onNext(StreamState.Searching)
        
        val job = scope.launch {
            delay(1000) // Small delay before first fetch
            
            while (isActive) {
                try {
                    println("$TAG: Fetching glucose data...")
                    val result = glucoseRepository.getLatestGlucose()
                    result.fold(
                        onSuccess = { glucoseEntry ->
                            if (GlucoseUtils.isDataStale(glucoseEntry)) {
                                println("$TAG: Data is stale, emitting NotAvailable")
                                emitter.onNext(StreamState.NotAvailable)
                            } else {
                                val glucoseValue = glucoseEntry.getGlucoseValue()
                                println("$TAG: Publishing glucose data: $glucoseValue mg/dl")
                                
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to glucoseValue.toDouble())
                                )
                                
                                println("$TAG: Emitting StreamState.Streaming with value: $glucoseValue")
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            println("$TAG: Error fetching glucose: ${exception.message}")
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
                    println("$TAG: Exception in glucose monitoring: ${e.message}")
                    e.printStackTrace()
                    emitter.onNext(StreamState.NotAvailable)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            println("$TAG: Stream cancelled")
            job.cancel()
        }
    }
}

class TimeSinceDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "time_since") {
    
    companion object {
        private const val TAG = "TimeSinceDataType"
        private const val REFRESH_INTERVAL = 1000L // 1 second for UI updates
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
        println("$TAG: startStream called")
        emitter.onNext(StreamState.Searching)
        
        val job = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getLatestGlucose()
                    result.fold(
                        onSuccess = { glucoseEntry ->
                            if (GlucoseUtils.isDataStale(glucoseEntry)) {
                                emitter.onNext(StreamState.NotAvailable)
                            } else {
                                val timeSince = glucoseEntry.getTimeSinceSeconds()
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to timeSince.toDouble())
                                )
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            println("$TAG: Error: ${exception.message}")
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
                    println("$TAG: Exception: ${e.message}")
                    emitter.onNext(StreamState.NotAvailable)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            job.cancel()
        }
    }
}

class TimeSinceFormattedDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "time_since_formatted") {
    
    companion object {
        private const val TAG = "TimeSinceFormattedDataType"
        private const val REFRESH_INTERVAL = 1000L // 1 second for UI updates
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
        println("$TAG: startStream called")
        emitter.onNext(StreamState.Searching)
        
        val job = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getLatestGlucose()
                    result.fold(
                        onSuccess = { glucoseEntry ->
                            if (GlucoseUtils.isDataStale(glucoseEntry)) {
                                emitter.onNext(StreamState.NotAvailable)
                            } else {
                                // Return formatted time in h:m:s format as numeric
                                val timeSinceFormattedNumeric = glucoseEntry.getTimeSinceFormattedNumeric()
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to timeSinceFormattedNumeric)
                                )
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            println("$TAG: Error: ${exception.message}")
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
                    println("$TAG: Exception: ${e.message}")
                    emitter.onNext(StreamState.NotAvailable)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            job.cancel()
        }
    }
}

class DirectionArrowDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "direction_arrow") {
    
    companion object {
        private const val TAG = "DirectionArrowDataType"
        private const val REFRESH_INTERVAL = 1000L // 1 second for UI updates
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
        println("$TAG: startStream called")
        emitter.onNext(StreamState.Searching)
        
        val job = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getLatestGlucose()
                    result.fold(
                        onSuccess = { glucoseEntry ->
                            if (GlucoseUtils.isDataStale(glucoseEntry)) {
                                emitter.onNext(StreamState.NotAvailable)
                            } else {
                                val direction = glucoseEntry.getDirectionArrow()
                                // Convert direction to numeric for display
                                val directionValue = when (direction) {
                                    "↑↑" -> 7.0
                                    "↑" -> 6.0
                                    "↗" -> 5.0
                                    "→" -> 4.0
                                    "↘" -> 3.0
                                    "↓" -> 2.0
                                    "↓↓" -> 1.0
                                    else -> 0.0
                                }
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to directionValue)
                                )
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            println("$TAG: Error: ${exception.message}")
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
                    println("$TAG: Exception: ${e.message}")
                    emitter.onNext(StreamState.NotAvailable)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            job.cancel()
        }
    }
}

class Delta15mMgDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "delta_15m_mg") {
    
    companion object {
        private const val TAG = "Delta15mMgDataType"
        private const val REFRESH_INTERVAL = 1000L // 1 second for UI updates
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
        println("$TAG: startStream called")
        emitter.onNext(StreamState.Searching)
        
        val job = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isEmpty()) {
                                emitter.onNext(StreamState.NotAvailable)
                            } else {
                                val latestEntry = entries.first()
                                if (GlucoseUtils.isDataStale(latestEntry)) {
                                    emitter.onNext(StreamState.NotAvailable)
                                } else {
                                    val delta = GlucoseUtils.calculateDelta15mMg(entries)
                                    val dataPoint = DataPoint(
                                        dataTypeId = dataTypeId,
                                        values = mapOf(DataType.Field.SINGLE to delta)
                                    )
                                    emitter.onNext(StreamState.Streaming(dataPoint))
                                }
                            }
                        },
                        onFailure = { exception ->
                            println("$TAG: Error: ${exception.message}")
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
                    println("$TAG: Exception: ${e.message}")
                    emitter.onNext(StreamState.NotAvailable)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            job.cancel()
        }
    }
}

class Delta15mMmolDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "delta_15m_mmol") {
    
    companion object {
        private const val TAG = "Delta15mMmolDataType"
        private const val REFRESH_INTERVAL = 1000L // 1 second for UI updates
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
        println("$TAG: startStream called")
        emitter.onNext(StreamState.Searching)
        
        val job = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isEmpty()) {
                                emitter.onNext(StreamState.NotAvailable)
                            } else {
                                val latestEntry = entries.first()
                                if (GlucoseUtils.isDataStale(latestEntry)) {
                                    emitter.onNext(StreamState.NotAvailable)
                                } else {
                                    val delta = GlucoseUtils.calculateDelta15mMmol(entries)
                                    val dataPoint = DataPoint(
                                        dataTypeId = dataTypeId,
                                        values = mapOf(DataType.Field.SINGLE to delta)
                                    )
                                    emitter.onNext(StreamState.Streaming(dataPoint))
                                }
                            }
                        },
                        onFailure = { exception ->
                            println("$TAG: Error: ${exception.message}")
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
                    println("$TAG: Exception: ${e.message}")
                    emitter.onNext(StreamState.NotAvailable)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            job.cancel()
        }
    }
    
    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        println("$TAG: Starting 15m mmol view with config $config")
        val configJob = CoroutineScope(Dispatchers.IO).launch {
            // Use a format that supports decimal display
            emitter.onNext(UpdateGraphicConfig(formatDataTypeId = DataType.Type.SPEED))
            awaitCancellation()
        }
        emitter.setCancellable {
            configJob.cancel()
        }
    }
}

class Delta5mMgDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "delta_5m_mg") {
    
    companion object {
        private const val TAG = "Delta5mMgDataType"
        private const val REFRESH_INTERVAL = 1000L // 1 second for UI updates
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
        println("$TAG: startStream called")
        emitter.onNext(StreamState.Searching)
        
        val job = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isEmpty()) {
                                emitter.onNext(StreamState.NotAvailable)
                            } else {
                                val latestEntry = entries.first()
                                if (GlucoseUtils.isDataStale(latestEntry)) {
                                    emitter.onNext(StreamState.NotAvailable)
                                } else {
                                    val delta = GlucoseUtils.calculateDelta5mMg(entries)
                                    val dataPoint = DataPoint(
                                        dataTypeId = dataTypeId,
                                        values = mapOf(DataType.Field.SINGLE to delta)
                                    )
                                    emitter.onNext(StreamState.Streaming(dataPoint))
                                }
                            }
                        },
                        onFailure = { exception ->
                            println("$TAG: Error: ${exception.message}")
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
                    println("$TAG: Exception: ${e.message}")
                    emitter.onNext(StreamState.NotAvailable)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            job.cancel()
        }
    }
}

class Delta5mMmolDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "delta_5m_mmol") {
    
    companion object {
        private const val TAG = "Delta5mMmolDataType"
        private const val REFRESH_INTERVAL = 1000L // 1 second for UI updates
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
        println("$TAG: startStream called")
        emitter.onNext(StreamState.Searching)
        
        val job = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isEmpty()) {
                                emitter.onNext(StreamState.NotAvailable)
                            } else {
                                val latestEntry = entries.first()
                                if (GlucoseUtils.isDataStale(latestEntry)) {
                                    emitter.onNext(StreamState.NotAvailable)
                                } else {
                                    val delta = GlucoseUtils.calculateDelta5mMmol(entries)
                                    val dataPoint = DataPoint(
                                        dataTypeId = dataTypeId,
                                        values = mapOf(DataType.Field.SINGLE to delta)
                                    )
                                    emitter.onNext(StreamState.Streaming(dataPoint))
                                }
                            }
                        },
                        onFailure = { exception ->
                            println("$TAG: Error: ${exception.message}")
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
                    println("$TAG: Exception: ${e.message}")
                    emitter.onNext(StreamState.NotAvailable)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            job.cancel()
        }
    }
    
    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        println("$TAG: Starting 5m mmol view with config $config")
        val configJob = CoroutineScope(Dispatchers.IO).launch {
            // Use a format that supports decimal display
            emitter.onNext(UpdateGraphicConfig(formatDataTypeId = DataType.Type.SPEED))
            awaitCancellation()
        }
        emitter.setCancellable {
            configJob.cancel()
        }
    }
}

class GlucoseMmolDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "glucose_mmol") {
    
    companion object {
        private const val TAG = "GlucoseMmolDataType"
        private const val REFRESH_INTERVAL = 1000L // 1 second for UI updates
        private const val MGDL_TO_MMOL_CONVERSION = 18.0
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
        println("$TAG: startStream called")
        
        // Immediately emit searching state
        emitter.onNext(StreamState.Searching)
        
        val job = scope.launch {
            delay(1000) // Small delay before first fetch
            
            while (isActive) {
                try {
                    println("$TAG: Fetching glucose data...")
                    val result = glucoseRepository.getLatestGlucose()
                    result.fold(
                        onSuccess = { glucoseEntry ->
                            if (GlucoseUtils.isDataStale(glucoseEntry)) {
                                println("$TAG: Data is stale, emitting NotAvailable")
                                emitter.onNext(StreamState.NotAvailable)
                            } else {
                                val glucoseValueMgdl = glucoseEntry.getGlucoseValue()
                                val glucoseValueMmol = glucoseValueMgdl / MGDL_TO_MMOL_CONVERSION
                                // Format to exactly 1 decimal place as string, then convert to double
                                val formattedString = String.format("%.1f", glucoseValueMmol)
                                val displayValue = formattedString.toDouble()
                                println("$TAG: Raw mg/dL: $glucoseValueMgdl, Raw mmol/L: $glucoseValueMmol, Formatted: $formattedString, Display: $displayValue")
                                
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to displayValue)
                                )
                                
                                println("$TAG: Emitting StreamState.Streaming with value: $displayValue")
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            println("$TAG: Error fetching glucose: ${exception.message}")
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
                    println("$TAG: Exception in glucose monitoring: ${e.message}")
                    e.printStackTrace()
                    emitter.onNext(StreamState.NotAvailable)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            println("$TAG: Stream cancelled")
            job.cancel()
        }
    }
    
    // No startView implementation - uses standard numeric view
}


