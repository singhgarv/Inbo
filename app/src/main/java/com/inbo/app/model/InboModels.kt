package com.inbo.app.model

/**
 * Task item data representation in Inbo.
 */
data class InboTask(
    val id: Int,
    val title: String,
    val completed: Boolean = false,
    val category: String = "General",
    val dueTime: String? = null
)

/**
 * High-signal update / summary item for Inbox.
 * Real, calm productivity updates without fake AI claims.
 */
data class InboxUpdate(
    val id: Int,
    val title: String,
    val summary: String,
    val category: String,
    val timeAgo: String,
    val isPriority: Boolean = false,
    val isRead: Boolean = false
)

/**
 * Scheduled calendar event item.
 * Supports date association (e.g. "2026-11-16" or day "16"), start/end time range,
 * event type/category (e.g. "Meeting", "Focus Time", "Review"), and location.
 */
data class CalendarEvent(
    val id: Int,
    val title: String,
    val timeRange: String,
    val type: String,
    val location: String? = null,
    val date: String = "16",
    val isToday: Boolean = true
)

/**
 * Milestone or deadline item.
 * Supports due date, tag/category, and completion status.
 */
data class CalendarDeadline(
    val id: Int,
    val title: String,
    val dueDate: String,
    val tag: String,
    val completed: Boolean = false,
    val date: String? = null
)

/**
 * User preferences model representing customizable app settings.
 * Default values maintain Inbo's calm, distraction-free baseline.
 */
data class InboSettings(
    val darkModeEnabled: Boolean = false,
    val quietHoursEnabled: Boolean = true,
    val morningDigestEnabled: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val autoArchiveEnabled: Boolean = false,
    val defaultLaunchScreen: String = "Inbox"
)

/**
 * Captured notification entity representing incoming updates from supported channels (e.g. WhatsApp).
 * Only system-exposed notification metadata is stored; private database access is never attempted.
 */
data class CapturedNotification(
    val id: Long = 0,
    val notificationKey: String,
    val packageName: String,
    val senderOrTitle: String,
    val messageSnippet: String,
    val timestamp: Long,
    val isGroup: Boolean = false,
    val isSummary: Boolean = false,
    val isDismissed: Boolean = false,
    val isRead: Boolean = false
)
