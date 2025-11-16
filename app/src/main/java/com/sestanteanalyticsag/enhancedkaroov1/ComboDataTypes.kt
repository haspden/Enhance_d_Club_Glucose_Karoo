package com.sestanteanalyticsag.enhancedkaroov1

import android.content.Context
import android.widget.RemoteViews
import com.sestanteanalyticsag.enhancedkaroov1.R
import com.sestanteanalyticsag.enhancedkaroov1.data.GlucoseEntry
import com.sestanteanalyticsag.enhancedkaroov1.repository.GlucoseRepository
import com.sestanteanalyticsag.enhancedkaroov1.util.GlucoseUtils
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.*
import kotlin.math.roundToInt

// Combo 1: Left: Time Since (s) | 15m delta + arrow, Right: Value (mg/dL)
class Combo1MgDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "combo_1_mg") {
    
    companion object {
        private const val TAG = "Combo1MgDataType"
        private const val REFRESH_INTERVAL = 1000L
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
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
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to glucoseEntry.getGlucoseValue().toDouble())
                                )
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
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
        val remoteViews = RemoteViews(context.packageName, R.layout.combo_view)
        
        emitter.onNext(UpdateGraphicConfig(formatDataTypeId = DataType.Type.SPEED))
        
        val viewJob = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isNotEmpty()) {
                                val latest = entries.first()
                                if (!GlucoseUtils.isDataStale(latest)) {
                                    val value = latest.getGlucoseValue()
                                    val timeSince = latest.getTimeSinceSeconds()
                                    val delta15m = GlucoseUtils.calculateDelta15mMg(entries)
                                    val arrow = latest.getDirectionArrow()
                                    
                                    remoteViews.setTextViewText(R.id.combo_left, "${timeSince}s | ${if (delta15m >= 0) "+" else ""}${delta15m.toInt()} $arrow")
                                    emitter.updateView(remoteViews)
                                } else {
                                    remoteViews.setTextViewText(R.id.combo_left, "--")
                                    emitter.updateView(remoteViews)
                                }
                            }
                        },
                        onFailure = { exception ->
                            remoteViews.setTextViewText(R.id.combo_left, "--")
                            emitter.updateView(remoteViews)
                        }
                    )
                } catch (e: Exception) {
                    remoteViews.setTextViewText(R.id.combo_left, "--")
                    emitter.updateView(remoteViews)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            viewJob.cancel()
        }
    }
}

// Combo 1: Left: Time Since (s) | 15m delta + arrow, Right: Value (mmol/L)
class Combo1MmolDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "combo_1_mmol") {
    
    companion object {
        private const val TAG = "Combo1MmolDataType"
        private const val REFRESH_INTERVAL = 1000L
        private const val MGDL_TO_MMOL_CONVERSION = 18.0182
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
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
                                val glucoseValueMgdl = glucoseEntry.getGlucoseValue()
                                val glucoseValueMmol = (glucoseValueMgdl / MGDL_TO_MMOL_CONVERSION * 10.0).roundToInt() / 10.0
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to glucoseValueMmol)
                                )
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
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
        val remoteViews = RemoteViews(context.packageName, R.layout.combo_view)
        
        emitter.onNext(UpdateGraphicConfig(formatDataTypeId = DataType.Type.ELEVATION_GRADE))
        
        val viewJob = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isNotEmpty()) {
                                val latest = entries.first()
                                if (!GlucoseUtils.isDataStale(latest)) {
                                    val valueMgdl = latest.getGlucoseValue()
                                    val valueMmol = (valueMgdl / MGDL_TO_MMOL_CONVERSION * 10.0).roundToInt() / 10.0
                                    val timeSince = latest.getTimeSinceSeconds()
                                    val delta15m = GlucoseUtils.calculateDelta15mMmol(entries)
                                    val arrow = latest.getDirectionArrow()
                                    
                                    remoteViews.setTextViewText(R.id.combo_left, "${timeSince}s | ${if (delta15m >= 0) "+" else ""}${String.format("%.1f", delta15m)} $arrow")
                                    emitter.updateView(remoteViews)
                                } else {
                                    remoteViews.setTextViewText(R.id.combo_left, "--")
                                    emitter.updateView(remoteViews)
                                }
                            }
                        },
                        onFailure = { exception ->
                            remoteViews.setTextViewText(R.id.combo_left, "--")
                            emitter.updateView(remoteViews)
                        }
                    )
                } catch (e: Exception) {
                    remoteViews.setTextViewText(R.id.combo_left, "--")
                    emitter.updateView(remoteViews)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            viewJob.cancel()
        }
    }
}

// Combo 2: Left: Time Since (s) | 5m delta + arrow, Right: Value (mg/dL)
class Combo2MgDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "combo_2_mg") {
    
    companion object {
        private const val TAG = "Combo2MgDataType"
        private const val REFRESH_INTERVAL = 1000L
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
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
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to glucoseEntry.getGlucoseValue().toDouble())
                                )
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
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
        val remoteViews = RemoteViews(context.packageName, R.layout.combo_view)
        
        emitter.onNext(UpdateGraphicConfig(formatDataTypeId = DataType.Type.SPEED))
        
        val viewJob = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isNotEmpty()) {
                                val latest = entries.first()
                                if (!GlucoseUtils.isDataStale(latest)) {
                                    val value = latest.getGlucoseValue()
                                    val timeSince = latest.getTimeSinceSeconds()
                                    val delta5m = GlucoseUtils.calculateDelta5mMg(entries)
                                    val arrow = latest.getDirectionArrow()
                                    
                                    remoteViews.setTextViewText(R.id.combo_left, "${timeSince}s | ${if (delta5m >= 0) "+" else ""}${delta5m.toInt()} $arrow")
                                    emitter.updateView(remoteViews)
                                } else {
                                    remoteViews.setTextViewText(R.id.combo_left, "--")
                                    emitter.updateView(remoteViews)
                                }
                            }
                        },
                        onFailure = { exception ->
                            remoteViews.setTextViewText(R.id.combo_left, "--")
                            emitter.updateView(remoteViews)
                        }
                    )
                } catch (e: Exception) {
                    remoteViews.setTextViewText(R.id.combo_left, "--")
                    emitter.updateView(remoteViews)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            viewJob.cancel()
        }
    }
}

// Combo 2: Left: Time Since (s) | 5m delta + arrow, Right: Value (mmol/L)
class Combo2MmolDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "combo_2_mmol") {
    
    companion object {
        private const val TAG = "Combo2MmolDataType"
        private const val REFRESH_INTERVAL = 1000L
        private const val MGDL_TO_MMOL_CONVERSION = 18.0182
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
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
                                val glucoseValueMgdl = glucoseEntry.getGlucoseValue()
                                val glucoseValueMmol = (glucoseValueMgdl / MGDL_TO_MMOL_CONVERSION * 10.0).roundToInt() / 10.0
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to glucoseValueMmol)
                                )
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
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
        val remoteViews = RemoteViews(context.packageName, R.layout.combo_view)
        
        emitter.onNext(UpdateGraphicConfig(formatDataTypeId = DataType.Type.ELEVATION_GRADE))
        
        val viewJob = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isNotEmpty()) {
                                val latest = entries.first()
                                if (!GlucoseUtils.isDataStale(latest)) {
                                    val valueMgdl = latest.getGlucoseValue()
                                    val valueMmol = (valueMgdl / MGDL_TO_MMOL_CONVERSION * 10.0).roundToInt() / 10.0
                                    val timeSince = latest.getTimeSinceSeconds()
                                    val delta5m = GlucoseUtils.calculateDelta5mMmol(entries)
                                    val arrow = latest.getDirectionArrow()
                                    
                                    remoteViews.setTextViewText(R.id.combo_left, "${timeSince}s | ${if (delta5m >= 0) "+" else ""}${String.format("%.1f", delta5m)} $arrow")
                                    emitter.updateView(remoteViews)
                                } else {
                                    remoteViews.setTextViewText(R.id.combo_left, "--")
                                    emitter.updateView(remoteViews)
                                }
                            }
                        },
                        onFailure = { exception ->
                            remoteViews.setTextViewText(R.id.combo_left, "--")
                            emitter.updateView(remoteViews)
                        }
                    )
                } catch (e: Exception) {
                    remoteViews.setTextViewText(R.id.combo_left, "--")
                    emitter.updateView(remoteViews)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            viewJob.cancel()
        }
    }
}

// Combo 3: Left: Time Since (m) | 15m delta + arrow, Right: Value (mg/dL)
class Combo3MgDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "combo_3_mg") {
    
    companion object {
        private const val TAG = "Combo3MgDataType"
        private const val REFRESH_INTERVAL = 1000L
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
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
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to glucoseEntry.getGlucoseValue().toDouble())
                                )
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
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
        val remoteViews = RemoteViews(context.packageName, R.layout.combo_view)
        
        emitter.onNext(UpdateGraphicConfig(formatDataTypeId = DataType.Type.SPEED))
        
        val viewJob = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isNotEmpty()) {
                                val latest = entries.first()
                                if (!GlucoseUtils.isDataStale(latest)) {
                                    val value = latest.getGlucoseValue()
                                    val timeSince = latest.getTimeSinceFormatted()
                                    val delta15m = GlucoseUtils.calculateDelta15mMg(entries)
                                    val arrow = latest.getDirectionArrow()
                                    
                                    remoteViews.setTextViewText(R.id.combo_left, "$timeSince | ${if (delta15m >= 0) "+" else ""}${delta15m.toInt()} $arrow")
                                    emitter.updateView(remoteViews)
                                } else {
                                    remoteViews.setTextViewText(R.id.combo_left, "--")
                                    emitter.updateView(remoteViews)
                                }
                            }
                        },
                        onFailure = { exception ->
                            remoteViews.setTextViewText(R.id.combo_left, "--")
                            emitter.updateView(remoteViews)
                        }
                    )
                } catch (e: Exception) {
                    remoteViews.setTextViewText(R.id.combo_left, "--")
                    emitter.updateView(remoteViews)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            viewJob.cancel()
        }
    }
}

// Combo 3: Left: Time Since (m) | 15m delta + arrow, Right: Value (mmol/L)
class Combo3MmolDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "combo_3_mmol") {
    
    companion object {
        private const val TAG = "Combo3MmolDataType"
        private const val REFRESH_INTERVAL = 1000L
        private const val MGDL_TO_MMOL_CONVERSION = 18.0182
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
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
                                val glucoseValueMgdl = glucoseEntry.getGlucoseValue()
                                val glucoseValueMmol = (glucoseValueMgdl / MGDL_TO_MMOL_CONVERSION * 10.0).roundToInt() / 10.0
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to glucoseValueMmol)
                                )
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
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
        val remoteViews = RemoteViews(context.packageName, R.layout.combo_view)
        
        emitter.onNext(UpdateGraphicConfig(formatDataTypeId = DataType.Type.ELEVATION_GRADE))
        
        val viewJob = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isNotEmpty()) {
                                val latest = entries.first()
                                if (!GlucoseUtils.isDataStale(latest)) {
                                    val valueMgdl = latest.getGlucoseValue()
                                    val valueMmol = (valueMgdl / MGDL_TO_MMOL_CONVERSION * 10.0).roundToInt() / 10.0
                                    val timeSince = latest.getTimeSinceFormatted()
                                    val delta15m = GlucoseUtils.calculateDelta15mMmol(entries)
                                    val arrow = latest.getDirectionArrow()
                                    
                                    remoteViews.setTextViewText(R.id.combo_left, "$timeSince | ${if (delta15m >= 0) "+" else ""}${String.format("%.1f", delta15m)} $arrow")
                                    emitter.updateView(remoteViews)
                                } else {
                                    remoteViews.setTextViewText(R.id.combo_left, "--")
                                    emitter.updateView(remoteViews)
                                }
                            }
                        },
                        onFailure = { exception ->
                            remoteViews.setTextViewText(R.id.combo_left, "--")
                            emitter.updateView(remoteViews)
                        }
                    )
                } catch (e: Exception) {
                    remoteViews.setTextViewText(R.id.combo_left, "--")
                    emitter.updateView(remoteViews)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            viewJob.cancel()
        }
    }
}

// Combo 4: Left: Time Since (m) | 5m delta + arrow, Right: Value (mg/dL)
class Combo4MgDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "combo_4_mg") {
    
    companion object {
        private const val TAG = "Combo4MgDataType"
        private const val REFRESH_INTERVAL = 1000L
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
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
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to glucoseEntry.getGlucoseValue().toDouble())
                                )
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
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
        val remoteViews = RemoteViews(context.packageName, R.layout.combo_view)
        
        emitter.onNext(UpdateGraphicConfig(formatDataTypeId = DataType.Type.SPEED))
        
        val viewJob = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isNotEmpty()) {
                                val latest = entries.first()
                                if (!GlucoseUtils.isDataStale(latest)) {
                                    val value = latest.getGlucoseValue()
                                    val timeSince = latest.getTimeSinceFormatted()
                                    val delta5m = GlucoseUtils.calculateDelta5mMg(entries)
                                    val arrow = latest.getDirectionArrow()
                                    
                                    remoteViews.setTextViewText(R.id.combo_left, "$timeSince | ${if (delta5m >= 0) "+" else ""}${delta5m.toInt()} $arrow")
                                    emitter.updateView(remoteViews)
                                } else {
                                    remoteViews.setTextViewText(R.id.combo_left, "--")
                                    emitter.updateView(remoteViews)
                                }
                            }
                        },
                        onFailure = { exception ->
                            remoteViews.setTextViewText(R.id.combo_left, "--")
                            emitter.updateView(remoteViews)
                        }
                    )
                } catch (e: Exception) {
                    remoteViews.setTextViewText(R.id.combo_left, "--")
                    emitter.updateView(remoteViews)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            viewJob.cancel()
        }
    }
}

// Combo 4: Left: Time Since (m) | 5m delta + arrow, Right: Value (mmol/L)
class Combo4MmolDataType(
    private val scope: CoroutineScope,
    private val glucoseRepository: GlucoseRepository
) : DataTypeImpl("enhanced_glucose", "combo_4_mmol") {
    
    companion object {
        private const val TAG = "Combo4MmolDataType"
        private const val REFRESH_INTERVAL = 1000L
        private const val MGDL_TO_MMOL_CONVERSION = 18.0182
    }
    
    override fun startStream(emitter: Emitter<StreamState>) {
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
                                val glucoseValueMgdl = glucoseEntry.getGlucoseValue()
                                val glucoseValueMmol = (glucoseValueMgdl / MGDL_TO_MMOL_CONVERSION * 10.0).roundToInt() / 10.0
                                val dataPoint = DataPoint(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to glucoseValueMmol)
                                )
                                emitter.onNext(StreamState.Streaming(dataPoint))
                            }
                        },
                        onFailure = { exception ->
                            emitter.onNext(StreamState.NotAvailable)
                        }
                    )
                } catch (e: Exception) {
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
        val remoteViews = RemoteViews(context.packageName, R.layout.combo_view)
        
        emitter.onNext(UpdateGraphicConfig(formatDataTypeId = DataType.Type.ELEVATION_GRADE))
        
        val viewJob = scope.launch {
            delay(1000)
            
            while (isActive) {
                try {
                    val result = glucoseRepository.getGlucoseEntries()
                    result.fold(
                        onSuccess = { entries ->
                            if (entries.isNotEmpty()) {
                                val latest = entries.first()
                                if (!GlucoseUtils.isDataStale(latest)) {
                                    val valueMgdl = latest.getGlucoseValue()
                                    val valueMmol = (valueMgdl / MGDL_TO_MMOL_CONVERSION * 10.0).roundToInt() / 10.0
                                    val timeSince = latest.getTimeSinceFormatted()
                                    val delta5m = GlucoseUtils.calculateDelta5mMmol(entries)
                                    val arrow = latest.getDirectionArrow()
                                    
                                    remoteViews.setTextViewText(R.id.combo_left, "$timeSince | ${if (delta5m >= 0) "+" else ""}${String.format("%.1f", delta5m)} $arrow")
                                    emitter.updateView(remoteViews)
                                } else {
                                    remoteViews.setTextViewText(R.id.combo_left, "--")
                                    emitter.updateView(remoteViews)
                                }
                            }
                        },
                        onFailure = { exception ->
                            remoteViews.setTextViewText(R.id.combo_left, "--")
                            emitter.updateView(remoteViews)
                        }
                    )
                } catch (e: Exception) {
                    remoteViews.setTextViewText(R.id.combo_left, "--")
                    emitter.updateView(remoteViews)
                }
                
                delay(REFRESH_INTERVAL)
            }
        }
        
        emitter.setCancellable {
            viewJob.cancel()
        }
    }
}
