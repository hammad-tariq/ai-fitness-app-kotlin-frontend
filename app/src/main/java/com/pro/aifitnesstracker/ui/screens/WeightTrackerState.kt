package com.pro.aifitnesstracker.ui.screens

data class WeightLogEntry(
    val id: Long,
    val weightKg: Float,
    val loggedAtMillis: Long
)

data class WeightTrackerState(
    val currentWeightKg: Float? = null,
    val targetWeightKg: Float? = null,
    val logs: List<WeightLogEntry> = emptyList()
) {
    fun withLog(weightKg: Float, atMillis: Long = System.currentTimeMillis()): WeightTrackerState {
        val entry = WeightLogEntry(
            id = atMillis,
            weightKg = weightKg,
            loggedAtMillis = atMillis
        )
        return copy(
            currentWeightKg = weightKg,
            logs = (listOf(entry) + logs).sortedByDescending { it.loggedAtMillis }
        )
    }

    fun withoutLogs(ids: Set<Long>): WeightTrackerState {
        val remaining = logs.filterNot { it.id in ids }
        return copy(
            logs = remaining,
            currentWeightKg = remaining.firstOrNull()?.weightKg ?: currentWeightKg
        )
    }
}
