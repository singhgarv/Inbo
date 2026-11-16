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
 */
data class CalendarEvent(
    val id: Int,
    val title: String,
    val timeRange: String,
    val type: String,
    val location: String? = null,
    val isToday: Boolean = true
)

/**
 * Milestone or deadline item.
 */
data class CalendarDeadline(
    val id: Int,
    val title: String,
    val dueDate: String,
    val tag: String,
    val completed: Boolean = false
)
