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
import com.inbo.app.ui.components.*
import com.inbo.app.ui.theme.*

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    // Preference States
    var quietHoursEnabled by remember { mutableStateOf(true) }
    var morningDigestEnabled by remember { mutableStateOf(true) }
    var taskRemindersEnabled by remember { mutableStateOf(true) }
    var hapticFeedbackEnabled by remember { mutableStateOf(true) }
    var autoArchiveEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        InboTopBar()

        Spacer(modifier = Modifier.height(28.dp))

        InboScreenHeader(
            eyebrow = "SETTINGS",
            title = "Make Inbo yours.",
            subtitle = "Tune quiet hours, alerts, and productivity preferences."
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile card
        InboCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(InboSoftPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = InboPurple
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Garv Singh",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = InboInk
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "garv@inbo.app",
                        fontSize = 13.sp,
                        color = InboMuted
                    )
                }

                InboBadge(
                    text = "Calm Active",
                    containerColor = InboSoftPurple,
                    contentColor = InboPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Notification Preferences Section
        Text(
            text = "Notification Controls",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InboInk
        )

        Spacer(modifier = Modifier.height(12.dp))

        InboCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsSwitchRow(
                    title = "Quiet Hours",
                    subtitle = "Mute non-critical alerts between 10:00 PM - 8:00 AM",
                    checked = quietHoursEnabled,
                    onCheckedChange = { quietHoursEnabled = it }
                )

                HorizontalDivider(color = InboDivider, thickness = 1.dp)

                SettingsSwitchRow(
                    title = "Morning Digest",
                    subtitle = "Receive a single curated summary at 8:30 AM",
                    checked = morningDigestEnabled,
                    onCheckedChange = { morningDigestEnabled = it }
                )

                HorizontalDivider(color = InboDivider, thickness = 1.dp)

                SettingsSwitchRow(
                    title = "Task Deadlines",
                    subtitle = "Remind 30 minutes before task due time",
                    checked = taskRemindersEnabled,
                    onCheckedChange = { taskRemindersEnabled = it }
                )

                HorizontalDivider(color = InboDivider, thickness = 1.dp)

                SettingsSwitchRow(
                    title = "Haptic Feedback",
                    subtitle = "Subtle tactile tap when completing a task",
                    checked = hapticFeedbackEnabled,
                    onCheckedChange = { hapticFeedbackEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // App Preferences Section
        Text(
            text = "App Behavior",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InboInk
        )

        Spacer(modifier = Modifier.height(12.dp))

        InboCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsSwitchRow(
                    title = "Auto-archive Completed",
                    subtitle = "Automatically archive completed tasks after 24 hours",
                    checked = autoArchiveEnabled,
                    onCheckedChange = { autoArchiveEnabled = it }
                )

                HorizontalDivider(color = InboDivider, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Default Launch Screen",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InboInk
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Screen displayed when opening Inbo",
                            fontSize = 12.sp,
                            color = InboMuted
                        )
                    }

                    InboBadge(
                        text = "Inbox",
                        containerColor = InboSoftPurple,
                        contentColor = InboPurple
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // About Inbo Section
        InboCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "inbo",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = InboInk,
                        letterSpacing = (-0.5).sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(bottom = 5.dp, start = 2.dp)
                            .size(5.dp)
                            .background(InboPurple, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "LESS NOISE. MORE LIFE.",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = InboMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Version 1.0.0",
                    fontSize = 12.sp,
                    color = InboMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Inbo is designed to protect your attention,\neliminating notification chaos with calm, focused surfaces.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = InboMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = InboInk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = InboMuted
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = InboPurple,
                uncheckedThumbColor = InboSubtle,
                uncheckedTrackColor = InboBackground
            )
        )
    }
}
