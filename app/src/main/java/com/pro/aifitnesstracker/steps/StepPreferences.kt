package com.pro.aifitnesstracker.steps

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun getSteps(): Int {
        ensureToday()
        return prefs.getInt(KEY_STEPS, 0)
    }

    fun setSteps(steps: Int) {
        ensureToday()
        prefs.edit().putInt(KEY_STEPS, steps.coerceAtLeast(0)).apply()
    }

    fun getGoal(): Int = prefs.getInt(KEY_GOAL, DEFAULT_GOAL)

    fun setGoal(goal: Int) {
        prefs.edit().putInt(KEY_GOAL, goal.coerceAtLeast(1_000)).apply()
    }

    fun getBaseline(): Float? {
        ensureToday()
        if (!prefs.contains(KEY_BASELINE)) return null
        return prefs.getFloat(KEY_BASELINE, 0f)
    }

    fun setBaseline(baseline: Float) {
        ensureToday()
        prefs.edit().putFloat(KEY_BASELINE, baseline).apply()
    }

    fun clearBaseline() {
        prefs.edit().remove(KEY_BASELINE).apply()
    }

    fun isTrackingEnabled(): Boolean = prefs.getBoolean(KEY_TRACKING, false)

    fun setTrackingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TRACKING, enabled).apply()
    }

    private fun ensureToday() {
        val today = todayKey()
        val stored = prefs.getString(KEY_DATE, null)
        if (stored != today) {
            prefs.edit()
                .putString(KEY_DATE, today)
                .putInt(KEY_STEPS, 0)
                .remove(KEY_BASELINE)
                .apply()
        }
    }

    companion object {
        private const val PREFS = "step_counter_prefs"
        private const val KEY_STEPS = "steps"
        private const val KEY_GOAL = "goal"
        private const val KEY_BASELINE = "baseline"
        private const val KEY_DATE = "date"
        private const val KEY_TRACKING = "tracking"
        const val DEFAULT_GOAL = 10_000
    }
}
