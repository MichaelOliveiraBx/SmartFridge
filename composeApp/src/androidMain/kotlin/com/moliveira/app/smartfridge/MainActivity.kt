package com.moliveira.app.smartfridge

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.moliveira.app.smartfridge.modules.notification.NotificationServicePlatform
import com.moliveira.app.smartfridge.notification.NotificationHelper
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    companion object {
        const val NOTIF_CHANNEL_ID = "notif_channel_id"
    }

    private val notificationService: NotificationServicePlatform by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannel()

        setContent {
            CompositionLocalProvider(
                LocalActivity provides this@MainActivity
            ) {
                App()
            }

            val askNotificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                notificationService.onPermissionResult(isGranted)
            }

            LaunchedEffect(Unit) {
                notificationService.askForPermissionEvent.collect {
                    Napier.d("askForPermissionEvent")
                    askNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val askCameraPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                notificationService.onPermissionResult(isGranted)
            }

            LaunchedEffect(Unit) {
                askCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Smart Fridge"
            val descriptionText = "Reminder to claim free coins"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIF_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

val LocalActivity = compositionLocalOf<Activity> {
    error("No Activity provided")
}
