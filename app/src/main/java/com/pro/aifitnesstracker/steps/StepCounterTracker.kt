package com.pro.aifitnesstracker.steps

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class StepCounterUiState(
    val steps: Int = 0,
    val goal: Int = StepPreferences.DEFAULT_GOAL,
    val isTracking: Boolean = false,
    val sensorAvailable: Boolean = true
) {
    val progress: Float
        get() = if (goal <= 0) 0f else (steps.toFloat() / goal).coerceIn(0f, 1f)

    val calories: Int
        get() = (steps * 0.04f).roundToInt()

    val distanceKm: Float
        get() = steps * 0.00078f

    val activeMinutes: Int
        get() = (steps / 100f).roundToInt()
}

object StepCounterTracker {
    private val _state = MutableStateFlow(StepCounterUiState())
    val state: StateFlow<StepCounterUiState> = _state.asStateFlow()

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val prefs = StepPreferences(context.applicationContext)
        _state.value = StepCounterUiState(
            steps = prefs.getSteps(),
            goal = prefs.getGoal(),
            isTracking = prefs.isTrackingEnabled()
        )
        initialized = true
    }

    fun updateSteps(steps: Int) {
        _state.value = _state.value.copy(steps = steps.coerceAtLeast(0))
    }

    fun updateGoal(context: Context, goal: Int) {
        val prefs = StepPreferences(context.applicationContext)
        prefs.setGoal(goal)
        _state.value = _state.value.copy(goal = prefs.getGoal())
        if (_state.value.isTracking) {
            // Refresh notification with new goal context
            start(context.applicationContext)
        }
    }

    fun setSensorAvailable(available: Boolean) {
        _state.value = _state.value.copy(sensorAvailable = available)
    }

    fun setTracking(tracking: Boolean) {
        _state.value = _state.value.copy(isTracking = tracking)
    }

    fun start(context: Context) {
        val app = context.applicationContext
        init(app)
        StepPreferences(app).setTrackingEnabled(true)
        setTracking(true)
        val intent = Intent(app, StepCounterService::class.java).apply {
            action = StepCounterService.ACTION_START
        }
        ContextCompat.startForegroundService(app, intent)
    }

    fun stop(context: Context) {
        val app = context.applicationContext
        StepPreferences(app).setTrackingEnabled(false)
        setTracking(false)
        val intent = Intent(app, StepCounterService::class.java).apply {
            action = StepCounterService.ACTION_STOP
        }
        app.startService(intent)
    }

    fun ensureRunningIfEnabled(context: Context) {
        init(context)
        if (StepPreferences(context.applicationContext).isTrackingEnabled()) {
            start(context)
        }
    }
}
