package com.pro.aifitnesstracker.steps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.pro.aifitnesstracker.MainActivity
import com.pro.aifitnesstracker.R

class StepCounterService : Service(), SensorEventListener {

    private lateinit var prefs: StepPreferences
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var usingDetector = false
    private var lastNotifiedSteps = -1

    override fun onCreate() {
        super.onCreate()
        prefs = StepPreferences(this)
        StepCounterTracker.init(this)
        createChannel()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> startTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        val steps = prefs.getSteps()
        val notification = buildNotification(steps)
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            } else {
                0
            }
        )
        StepCounterTracker.setTracking(true)
        StepCounterTracker.updateSteps(steps)

        val manager = sensorManager
        if (manager == null) {
            StepCounterTracker.setSensorAvailable(false)
            return
        }

        when {
            stepCounterSensor != null -> {
                usingDetector = false
                StepCounterTracker.setSensorAvailable(true)
                manager.registerListener(
                    this,
                    stepCounterSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            stepDetectorSensor != null -> {
                usingDetector = true
                StepCounterTracker.setSensorAvailable(true)
                manager.registerListener(
                    this,
                    stepDetectorSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            else -> {
                StepCounterTracker.setSensorAvailable(false)
                updateNotification(steps)
            }
        }
    }

    private fun stopTracking() {
        sensorManager?.unregisterListener(this)
        prefs.setTrackingEnabled(false)
        StepCounterTracker.setTracking(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSinceBoot = event.values[0]
                var baseline = prefs.getBaseline()
                if (baseline == null) {
                    // Continue from saved today steps if service restarted mid-day
                    baseline = totalSinceBoot - prefs.getSteps()
                    prefs.setBaseline(baseline)
                }
                val todaySteps = (totalSinceBoot - baseline).toInt().coerceAtLeast(0)
                publishSteps(todaySteps)
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                val todaySteps = prefs.getSteps() + 1
                publishSteps(todaySteps)
            }
        }
    }

    private fun publishSteps(steps: Int) {
        prefs.setSteps(steps)
        StepCounterTracker.updateSteps(steps)
        if (steps != lastNotifiedSteps) {
            lastNotifiedSteps = steps
            updateNotification(steps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.step_notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.step_notification_channel_desc)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(steps: Int): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val goal = prefs.getGoal()
        val title = getString(R.string.step_notification_title)
        val text = getString(R.string.step_notification_text, steps, goal)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_steps)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(openApp)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(steps))
    }

    companion object {
        const val ACTION_START = "com.pro.aifitnesstracker.steps.START"
        const val ACTION_STOP = "com.pro.aifitnesstracker.steps.STOP"
        private const val CHANNEL_ID = "step_counter_channel"
        private const val NOTIFICATION_ID = 1101
    }
}
