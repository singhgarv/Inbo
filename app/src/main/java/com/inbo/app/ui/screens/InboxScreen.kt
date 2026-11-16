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
import com.inbo.app.model.InboxUpdate
import com.inbo.app.ui.components.*
import com.inbo.app.ui.theme.*

@Composable
fun InboxScreen(
    remainingTasksCount: Int,
    onNavigateToTasks: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val updates = remember {
        mutableStateListOf(
            InboxUpdate(
                id = 1,
                title = "Sprint Roadmap Finalized",
                summary = "Key mobile milestones set for Q4 release. Core focus on calm UX and responsiveness.",
                category = "Roadmap",
                timeAgo = "15m ago",
                isPriority = true,
                isRead = false
            ),
            InboxUpdate(
                id = 2,
                title = "Design Tokens Reviewed",
                summary = "Subtle purple accent (#7165E8) and soft card styling standardized across all views.",
                category = "Design",
                timeAgo = "1h ago",
                isPriority = false,
                isRead = false
            ),
            InboxUpdate(
                id = 3,
                title = "Daily Focus Digest",
                summary = "Quiet hours scheduled for 10:00 PM tonight. You have scheduled focus time at 11:00 AM.",
                category = "Digest",
                timeAgo = "3h ago",
                isPriority = false,
                isRead = true
            )
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }

    val filteredUpdates = when (selectedFilter) {
        "Priority" -> updates.filter { it.isPriority }
        "Unread" -> updates.filter { !it.isRead }
        else -> updates
    }

    val unreadCount = updates.count { !it.isRead }

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
                title = "IMPORTANT",
                value = "$unreadCount",
                subtitle = if (unreadCount == 1) "New update" else "New updates",
                modifier = Modifier.weight(1f),
                accentColor = InboPurple
            )

            InboStatCard(
                title = "YOUR TASKS",
                value = "$remainingTasksCount",
                subtitle = "To complete",
                modifier = Modifier.weight(1f),
                accentColor = InboSoftPurple,
                onClick = onNavigateToTasks
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
                text = "Recent updates",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InboInk
            )

            if (unreadCount > 0) {
                Text(
                    text = "Mark all read",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InboPurple,
                    modifier = Modifier.clickable {
                        updates.indices.forEach { idx ->
                            updates[idx] = updates[idx].copy(isRead = true)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Unread", "Priority").forEach { filter ->
                InboFilterChip(
                    text = filter,
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Updates List
        if (filteredUpdates.isEmpty()) {
            InboCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("✦", fontSize = 24.sp, color = InboPurple)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No updates in this filter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = InboInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Everything in this category has been reviewed.",
                        fontSize = 13.sp,
                        color = InboMuted
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredUpdates.forEach { update ->
                    InboCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val idx = updates.indexOfFirst { it.id == update.id }
                            if (idx != -1) {
                                updates[idx] = updates[idx].copy(isRead = !updates[idx].isRead)
                            }
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
                                InboBadge(
                                    text = update.category,
                                    containerColor = if (update.isPriority) InboSoftPurple else InboLightPurple,
                                    contentColor = InboPurple
                                )
                                if (!update.isRead) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(InboPurple, CircleShape)
                                    )
                                }
                            }

                            Text(
                                text = update.timeAgo,
                                fontSize = 11.sp,
                                color = InboMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = update.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InboInk
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = update.summary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = InboMuted
                        )
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
                        .background(InboSoftPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦", fontSize = 22.sp, color = InboPurple)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "All caught up!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = InboInk
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "No urgent distractions right now.\nTake a breath and focus on what matters.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = InboMuted,
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
                color = InboSubtle
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
