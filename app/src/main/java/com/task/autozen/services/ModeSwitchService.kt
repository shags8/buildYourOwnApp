package com.task.autozen.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.task.autozen.data.local.AppDatabase
import com.task.autozen.domain.repository.ModeRepositoryImpl
import kotlinx.coroutines.*

class ModeSwitchService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var audioManager: AudioManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private var currentMode: Int = AudioManager.RINGER_MODE_NORMAL

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                checkLocationAndSetMode(location)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (!hasRequiredPermissions()) {
            Log.e("ModeSwitchService", "Required permissions not granted. Stopping service.")
            stopSelf()
            return
        }

        startForeground(1, createNotification())
        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "UPDATE_MODE" -> {
                Log.d("ModeSwitchService", "Received UPDATE_MODE action")
                requestLocationAndUpdateMode()
            }
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "ModeSwitchService"
        val channel = NotificationChannel(
            channelId, "Mode Switch Service", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("AutoZen Running")
            .setContentText("Monitoring your location for mode switching")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
            .build()
    }

    private fun startLocationUpdates() {
        // set time-interval for checking locations
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000)
            .setMinUpdateIntervalMillis(5_000)
            .build()

        if (!hasRequiredPermissions()) {
            stopSelf()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    private fun requestLocationAndUpdateMode() {
        if (!hasRequiredPermissions()) {
            Log.e("ModeSwitchService", "Location permission missing.")
            stopSelf()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let { checkLocationAndSetMode(it) }
        }.addOnFailureListener { e ->
            Log.e("ModeSwitchService", "Failed to get location: ${e.message}")
        }
    }

    private fun checkLocationAndSetMode(currentLocation: Location) {
        serviceScope.launch {
            val repository =
                ModeRepositoryImpl(AppDatabase.getInstance(applicationContext).savedLocationDao())
            val savedLocations = repository.getSavedLocations()

            for (savedLocation in savedLocations) {
                val savedLatLng = Location("").apply {
                    latitude = savedLocation.latitude
                    longitude = savedLocation.longitude
                }

                val distance = currentLocation.distanceTo(savedLatLng)

                if (distance <= savedLocation.radius) {
                    if (currentMode != savedLocation.mode) {
                        setPhoneMode(savedLocation.mode)
                        currentMode = savedLocation.mode
                    }
                    return@launch
                }
            }
            resetPhoneMode()
        }
    }

    private fun setPhoneMode(mode: Int) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            Log.e("ModeSwitchService", "DND permission is not granted.")
            stopSelf()
            return
        }

        try {
            if (currentMode == mode) return // Skip if already in the correct mode

            when (mode) {
                AudioManager.RINGER_MODE_VIBRATE -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    showToast("Phone set to Vibrate mode")
                }

                AudioManager.RINGER_MODE_SILENT -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    showToast("Phone set to Silent mode")
                }

                else -> {
                    Log.e("ModeSwitchService", "Invalid mode: $mode")
                }
            }
            currentMode = mode // Update the current mode state

        } catch (e: SecurityException) {
            Log.e("ModeSwitchService", "Failed to change ringer mode: ${e.message}")
        }
    }

    private fun resetPhoneMode() {
        if (currentMode == AudioManager.RINGER_MODE_NORMAL) return // Skip if already normal
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        showToast("Phone set to Normal mode")
        currentMode = AudioManager.RINGER_MODE_NORMAL
    }

    private fun hasRequiredPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.FOREGROUND_SERVICE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
