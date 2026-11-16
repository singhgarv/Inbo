package com.inbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inbo.app.intelligence.ExtractedInformation
import com.inbo.app.intelligence.NotificationCategory
import com.inbo.app.intelligence.RuleBasedIntelligenceEngine
import com.inbo.app.model.CapturedNotification
import com.inbo.app.ui.components.*
import com.inbo.app.ui.theme.*

/**
 * Format timestamp into calm relative time strings (e.g., "Just now", "2m ago", "1h ago", "Yesterday").
 */
fun formatRelativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val diff = now - timestamp
    if (diff < 0L) return "Just now"
    val seconds = diff / 1000L
    val minutes = seconds / 60L
    val hours = minutes / 60L
    val days = hours / 24L

    return when {
        minutes < 1L -> "Just now"
        minutes < 60L -> "${minutes}m ago"
        hours < 24L -> "${hours}h ago"
        days == 1L -> "Yesterday"
        days < 7L -> "${days}d ago"
        else -> "${days / 7}w ago"
    }
}

@Composable
fun InboxScreen(
    notifications: List<CapturedNotification>,
    remainingTasksCount: Int,
    onMarkAsRead: (id: Long) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onNavigateToTasks: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var inspectingNotification by remember { mutableStateOf<Pair<CapturedNotification, ExtractedInformation>?>(null) }

    // Instantiate local rule-based intelligence engine (100% offline, zero cloud API)
    val intelligenceEngine = remember { RuleBasedIntelligenceEngine() }

    // Pre-analyze notifications with local engine
    val analyzedNotifications = remember(notifications) {
        notifications.map { notif ->
            notif to intelligenceEngine.analyze(notif.senderOrTitle, notif.messageSnippet)
        }
    }

    val classifiedUpdates = remember(analyzedNotifications) {
        analyzedNotifications.filter { it.second.isClassified }
    }

    val filteredNotifications = when (selectedFilter) {
        "Unread" -> analyzedNotifications.filter { !it.first.isRead }
        "Groups" -> analyzedNotifications.filter { it.first.isGroup }
        "Important" -> classifiedUpdates
        else -> analyzedNotifications
    }

    val unreadCount = notifications.count { !it.isRead }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        InboTopBar()

        Spacer(modifier = Modifier.height(28.dp))

        InboScreenHeader(
            eyebrow = "YOUR INBOX",
            title = "Your day,\nwithout the noise.",
            subtitle = "The important things, all in one place."
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Summary Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InboStatCard(
                title = "UNREAD ALERTS",
                value = "$unreadCount",
                subtitle = if (unreadCount == 1) "Unread message" else "Unread messages",
                modifier = Modifier.weight(1f),
                accentColor = InboTheme.colors.accentPurple
            )

            InboStatCard(
                title = "IMPORTANT UPDATES",
                value = "${classifiedUpdates.size}",
                subtitle = if (classifiedUpdates.size == 1) "Recognized item" else "Recognized items",
                modifier = Modifier.weight(1f),
                accentColor = InboTheme.colors.accentPurple,
                onClick = { selectedFilter = "Important" }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section Title & Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedFilter == "Important") "Important Updates" else "WhatsApp Inbox",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InboTheme.colors.textPrimary
            )

            if (unreadCount > 0) {
                Text(
                    text = "Mark all read",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InboTheme.colors.accentPurple,
                    modifier = Modifier.clickable { onMarkAllAsRead() }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips: All, Important, Unread, Groups
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Important", "Unread", "Groups").forEach { filter ->
                InboFilterChip(
                    text = if (filter == "Important") "Important (${classifiedUpdates.size})" else filter,
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Notifications List or Clean Empty States
        if (notifications.isEmpty()) {
            // General empty state when no notifications have been captured yet
            InboCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(InboTheme.colors.softPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✦", fontSize = 22.sp, color = InboTheme.colors.accentPurple)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Your inbox is clear",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = InboTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "WhatsApp notifications captured by Inbo will appear here calmly, organized without group chat chatter.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = InboTheme.colors.textMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else if (filteredNotifications.isEmpty()) {
            // Filter-specific empty state (e.g. Unread, Groups, or Important has 0 items)
            InboCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("✦", fontSize = 24.sp, color = InboTheme.colors.accentPurple)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (selectedFilter) {
                            "Important" -> "No recognized updates"
                            "Unread" -> "No unread messages"
                            else -> "No group messages"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = InboTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (selectedFilter) {
                            "Important" -> "High-signal updates (exams, deadlines, assignments) will appear here automatically."
                            "Unread" -> "You are completely caught up!"
                            else -> "No messages from group chats captured."
                        },
                        fontSize = 13.sp,
                        color = InboTheme.colors.textMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredNotifications.forEach { (item, info) ->
                    InboCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = if (item.isRead) InboTheme.colors.cardSurfaceSubtle else InboTheme.colors.cardSurface,
                        onClick = {
                            if (!item.isRead) {
                                onMarkAsRead(item.id)
                            }
                            inspectingNotification = item to info
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Category badge or group indicator
                                if (info.isClassified) {
                                    InboBadge(
                                        text = info.category.badgeLabel,
                                        containerColor = if (info.category == NotificationCategory.CANCELLATION) {
                                            Color(0xFFFFEEEE)
                                        } else {
                                            InboTheme.colors.softPurple
                                        },
                                        contentColor = if (info.category == NotificationCategory.CANCELLATION) {
                                            Color(0xFFD32F2F)
                                        } else {
                                            InboTheme.colors.accentPurple
                                        }
                                    )
                                } else {
                                    InboBadge(
                                        text = if (item.isGroup) "Group" else "Direct",
                                        containerColor = if (item.isGroup) InboTheme.colors.cardSurfaceSubtle else InboTheme.colors.softPurple,
                                        contentColor = InboTheme.colors.accentPurple
                                    )
                                }

                                if (info.isClassified) {
                                    Text(
                                        text = "${info.confidence}% match",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = InboTheme.colors.textMuted
                                    )
                                }

                                if (!item.isRead) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(InboTheme.colors.accentPurple, CircleShape)
                                    )
                                }
                            }

                            Text(
                                text = formatRelativeTime(item.timestamp),
                                fontSize = 11.sp,
                                color = InboTheme.colors.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.senderOrTitle,
                            fontSize = 15.sp,
                            fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
                            color = InboTheme.colors.textPrimary
                        )

                        if (item.messageSnippet.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.messageSnippet,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = if (item.isRead) InboTheme.colors.textMuted else InboTheme.colors.textPrimary
                            )
                        }

                        // Structured Extraction Preview Chips (if extracted)
                        if (info.isClassified && (info.dateText != null || info.timeText != null || info.venue != null || info.subjectOrTopic != null)) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                info.subjectOrTopic?.let { subj ->
                                    InboBadge(
                                        text = subj,
                                        containerColor = InboTheme.colors.cardSurfaceSubtle,
                                        contentColor = InboTheme.colors.textPrimary
                                    )
                                }
                                info.dateText?.let { d ->
                                    InboBadge(
                                        text = d,
                                        containerColor = InboTheme.colors.cardSurfaceSubtle,
                                        contentColor = InboTheme.colors.accentPurple
                                    )
                                }
                                info.timeText?.let { t ->
                                    InboBadge(
                                        text = t,
                                        containerColor = InboTheme.colors.cardSurfaceSubtle,
                                        contentColor = InboTheme.colors.accentPurple
                                    )
                                }
                                info.venue?.let { v ->
                                    InboBadge(
                                        text = v,
                                        containerColor = InboTheme.colors.cardSurfaceSubtle,
                                        contentColor = InboTheme.colors.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Caught-up Calm Banner
        InboCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(InboTheme.colors.softPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦", fontSize = 22.sp, color = InboTheme.colors.accentPurple)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Less noise. More life.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = InboTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "No urgent distractions right now.\nTake a breath and focus on what matters.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = InboTheme.colors.textMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "LESS NOISE. MORE LIFE.",
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                color = InboTheme.colors.textSubtle
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Modal Sheet / Dialog to inspect extracted details and original notification text
    inspectingNotification?.let { (notif, info) ->
        AlertDialog(
            onDismissRequest = { inspectingNotification = null },
            title = {
                Text(
                    text = "Notification Details",
                    fontWeight = FontWeight.Bold,
                    color = InboTheme.colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Classification & Confidence
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InboBadge(
                            text = info.category.displayName,
                            containerColor = InboTheme.colors.softPurple,
                            contentColor = InboTheme.colors.accentPurple
                        )
                        Text(
                            text = "${info.confidence}% confidence",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InboTheme.colors.textMuted
                        )
                    }

                    HorizontalDivider(color = InboTheme.colors.divider, thickness = 1.dp)

                    // Extracted entities
                    info.subjectOrTopic?.let {
                        Text(
                            text = "Subject: $it",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = InboTheme.colors.textPrimary
                        )
                    }
                    info.actionItem?.let {
                        Text(
                            text = "Action: $it",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = InboTheme.colors.accentPurple
                        )
                    }
                    if (info.dateText != null || info.timeText != null) {
                        Text(
                            text = "Schedule: ${listOfNotNull(info.dateText, info.timeText).joinToString(" at ")}",
                            fontSize = 13.sp,
                            color = InboTheme.colors.textPrimary
                        )
                    }
                    info.venue?.let {
                        Text(
                            text = "Location / Venue: $it",
                            fontSize = 13.sp,
                            color = InboTheme.colors.textMuted
                        )
                    }

                    HorizontalDivider(color = InboTheme.colors.divider, thickness = 1.dp)

                    // Original notification text for user verification
                    Text(
                        text = "Original Notification",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = InboTheme.colors.textMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = notif.messageSnippet.ifEmpty { "(No snippet text)" },
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = InboTheme.colors.textPrimary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectingNotification = null }) {
                    Text("Done", color = InboTheme.colors.accentPurple, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = InboTheme.colors.cardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
