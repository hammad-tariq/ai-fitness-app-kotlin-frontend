package com.pro.aifitnesstracker.data

import android.content.Context
import java.util.UUID

object ClientIdStore {
    private const val Prefs = "fitness_client_prefs"
    private const val KeyClientId = "client_id"

    fun getOrCreate(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(Prefs, Context.MODE_PRIVATE)
        val existing = prefs.getString(KeyClientId, null)
        if (!existing.isNullOrBlank()) return existing
        val created = UUID.randomUUID().toString()
        prefs.edit().putString(KeyClientId, created).apply()
        return created
    }
}
