package com.pro.aifitnesstracker.data

import com.pro.aifitnesstracker.BuildConfig

object ApiConfig {
    val BASE_URL: String = BuildConfig.FITNESS_API_BASE_URL
    /** Client / food-scan API key from local.properties */
    val API_KEY: String = BuildConfig.FITNESS_API_KEY
    /** Admin API key from local.properties (server admin ops only) */
    val ADMIN_API_KEY: String = BuildConfig.FITNESS_ADMIN_API_KEY
}
