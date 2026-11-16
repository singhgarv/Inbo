package com.inbo.app.data.repository

import com.inbo.app.data.local.NotificationDao
import com.inbo.app.model.CapturedNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Repository managing captured notifications.
 *
 * Privacy Guarantees:
 * 1. Single source of truth: SQLite database.
 * 2. 72-Hour Retention: Expired records are pruned on initialization and subsequent writes.
 * 3. Hard Deletion: Dismissals result in permanent removal from disk.
 * 4. Zero Logging: Message contents are never output to logcat.
 */
class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val scope: CoroutineScope
) {
    private val _notifications = MutableStateFlow<List<CapturedNotification>>(emptyList())
    val notifications: StateFlow<List<CapturedNotification>> = _notifications.asStateFlow()

    // Lightweight live event stream for incoming notifications
    private val _incomingEvents = MutableSharedFlow<CapturedNotification>(extraBufferCapacity = 64)
    val incomingEvents: SharedFlow<CapturedNotification> = _incomingEvents.asSharedFlow()

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    init {
        // Prune expired notifications (> 72 hours) and load current records on repository initialization
        scope.launch {
            notificationDao.pruneExpiredNotifications()
            _notifications.value = notificationDao.getAllNotifications()
        }
    }

    fun onServiceConnectionChanged(connected: Boolean) {
        _isServiceConnected.value = connected
    }

    /**
     * Saves a captured notification to SQLite, runs retention pruning, and updates StateFlow.
     */
    fun onNotificationReceived(notification: CapturedNotification) {
        scope.launch {
            // Prune any expired notifications before persisting new one
            notificationDao.pruneExpiredNotifications(notification.timestamp)

            // Persist to SQLite
            notificationDao.insertOrUpdateNotification(notification)

            // Update in-memory state from database
            _notifications.value = notificationDao.getAllNotifications(notification.timestamp)

            // Emit live event
            _incomingEvents.tryEmit(notification)
        }
    }

    /**
     * Hard-deletes a notification permanently when dismissed in the system notification shade.
     * Updates repository StateFlow immediately.
     */
    fun onNotificationRemoved(notificationKey: String) {
        scope.launch {
            notificationDao.hardDeleteNotification(notificationKey)
            _notifications.value = notificationDao.getAllNotifications()
        }
    }

    /**
     * Marks an individual notification as read or unread and updates SQLite + StateFlow.
     */
    fun markNotificationAsRead(id: Long, isRead: Boolean = true) {
        scope.launch {
            notificationDao.markNotificationAsRead(id, isRead)
            _notifications.value = notificationDao.getAllNotifications()
        }
    }

    /**
     * Marks an individual notification as read or unread by key and updates SQLite + StateFlow.
     */
    fun markNotificationAsReadByKey(notificationKey: String, isRead: Boolean = true) {
        scope.launch {
            notificationDao.markNotificationAsReadByKey(notificationKey, isRead)
            _notifications.value = notificationDao.getAllNotifications()
        }
    }

    /**
     * Marks all notifications as read and updates SQLite + StateFlow.
     */
    fun markAllNotificationsAsRead() {
        scope.launch {
            notificationDao.markAllNotificationsAsRead()
            _notifications.value = notificationDao.getAllNotifications()
        }
    }

    /**
     * Manually triggers a retention prune cycle.
     */
    fun pruneExpiredNotifications(nowMs: Long = System.currentTimeMillis()) {
        scope.launch {
            notificationDao.pruneExpiredNotifications(nowMs)
            _notifications.value = notificationDao.getAllNotifications(nowMs)
        }
    }

    companion object {
        // Singleton holder to allow InboNotificationListenerService to bind to the repository instance
        @Volatile
        private var instance: NotificationRepository? = null

        fun getInstance(): NotificationRepository? = instance

        fun setInstance(repository: NotificationRepository) {
            instance = repository
        }
    }
}
