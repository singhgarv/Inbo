package com.inbo.app.notification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/**
 * Helper to check and navigate to Android's official Notification Listener access settings.
 * Only official system settings are used.
 */
object NotificationPermissionHelper {

    /**
     * Checks if notification listener permission has been granted to Inbo.
     */
    fun isNotificationAccessGranted(context: Context): Boolean {
        val packageName = context.packageName
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(packageName)
    }

    /**
     * Creates an Intent to open Android's official Notification Listener settings screen.
     */
    fun createNotificationAccessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
