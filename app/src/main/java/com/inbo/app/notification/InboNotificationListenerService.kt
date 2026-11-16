package com.inbo.app.notification

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.inbo.app.data.repository.NotificationRepository
import com.inbo.app.model.CapturedNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Android NotificationListenerService capturing high-signal notifications from WhatsApp.
 *
 * Requirements & Privacy Guarantees:
 * 1. Read-only: Does not interact with or send messages.
 * 2. Privacy-first: Notification titles, snippets, or message contents are never logged to logcat/console.
 * 3. Hard-deletion on dismissal: When dismissed from system tray, deleted permanently from SQLite.
 * 4. 72-Hour retention: Expired items are pruned automatically.
 * 5. Asynchronous: Does not block the system callback thread.
 */
class InboNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        // Supported WhatsApp package identifiers
        const val PACKAGE_WHATSAPP = "com.whatsapp"
        const val PACKAGE_WHATSAPP_BUSINESS = "com.whatsapp.w4b"

        private val SUPPORTED_PACKAGES = setOf(
            PACKAGE_WHATSAPP,
            PACKAGE_WHATSAPP_BUSINESS
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationRepository.getInstance()?.onServiceConnectionChanged(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotificationRepository.getInstance()?.onServiceConnectionChanged(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        if (!SUPPORTED_PACKAGES.contains(packageName)) {
            // Only process target WhatsApp notifications
            return
        }

        val notification = sbn.notification ?: return

        // Ignore ongoing / foreground service notifications (e.g. WhatsApp backup, call in progress)
        if ((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) {
            return
        }

        val extras: Bundle = notification.extras ?: Bundle()

        // Extract metadata safely without crashing on null or non-String CharSequences
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim() ?: ""

        val effectiveSnippet = when {
            bigText.isNotEmpty() -> bigText
            text.isNotEmpty() -> text
            else -> subText
        }

        // If both title and message are empty, drop the empty notification
        if (title.isEmpty() && effectiveSnippet.isEmpty()) {
            return
        }

        val isGroupConversation = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false) ||
                (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0

        val isGroupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0

        val captured = CapturedNotification(
            notificationKey = sbn.key ?: "${sbn.packageName}_${sbn.id}_${sbn.postTime}",
            packageName = packageName,
            senderOrTitle = title.ifEmpty { "WhatsApp" },
            messageSnippet = effectiveSnippet,
            timestamp = sbn.postTime,
            isGroup = isGroupConversation,
            isSummary = isGroupSummary,
            isDismissed = false
        )

        // Asynchronously forward to repository without blocking the system notification thread
        serviceScope.launch {
            NotificationRepository.getInstance()?.onNotificationReceived(captured)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val packageName = sbn.packageName ?: return
        if (!SUPPORTED_PACKAGES.contains(packageName)) return

        val key = sbn.key ?: return
        // Hard-deletes permanently via repository
        serviceScope.launch {
            NotificationRepository.getInstance()?.onNotificationRemoved(key)
        }
    }
}
