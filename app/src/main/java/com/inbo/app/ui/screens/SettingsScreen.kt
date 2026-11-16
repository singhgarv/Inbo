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
import com.inbo.app.model.InboSettings
import com.inbo.app.ui.components.*
import com.inbo.app.ui.theme.*

@Composable
fun SettingsScreen(
    settings: InboSettings,
    isNotificationAccessGranted: Boolean,
    onOpenNotificationAccessSettings: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onQuietHoursChange: (Boolean) -> Unit,
    onMorningDigestChange: (Boolean) -> Unit,
    onTaskRemindersChange: (Boolean) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onAutoArchiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
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
                        .background(InboTheme.colors.softPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = InboTheme.colors.accentPurple
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Garv Singh",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = InboTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "garv@inbo.app",
                        fontSize = 13.sp,
                        color = InboTheme.colors.textMuted
                    )
                }

                InboBadge(
                    text = "Calm Active",
                    containerColor = InboTheme.colors.softPurple,
                    contentColor = InboTheme.colors.accentPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Appearance Section
        Text(
            text = "Appearance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InboTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        InboCard(modifier = Modifier.fillMaxWidth()) {
            SettingsSwitchRow(
                title = "Dark Mode",
                subtitle = "Deep charcoal theme with calm, high-contrast accents",
                checked = settings.darkModeEnabled,
                onCheckedChange = onDarkModeChange
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Integrations & Noise Reduction Section
        Text(
            text = "Integrations & Noise Reduction",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InboTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        InboCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "WhatsApp Integration",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InboTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Capture incoming notifications locally to summarize high-priority messages without chat noise.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = InboTheme.colors.textMuted
                        )
                    }

                    InboBadge(
                        text = if (isNotificationAccessGranted) "Connected" else "Setup Needed",
                        containerColor = if (isNotificationAccessGranted) InboSuccessSoft else InboTheme.colors.softPurple,
                        contentColor = if (isNotificationAccessGranted) InboSuccess else InboTheme.colors.accentPurple
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNotificationAccessGranted) {
                            "Android Notification Listener access is active."
                        } else {
                            "Requires Android Notification Listener permission."
                        },
                        fontSize = 11.sp,
                        color = InboTheme.colors.textMuted,
                        modifier = Modifier.weight(1f).padding(end = 10.dp)
                    )

                    InboButton(
                        text = if (isNotificationAccessGranted) "Manage" else "Enable",
                        onClick = onOpenNotificationAccessSettings,
                        modifier = Modifier.height(38.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Notification Preferences Section
        Text(
            text = "Notification Controls",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InboTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        InboCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsSwitchRow(
                    title = "Quiet Hours",
                    subtitle = "Mute non-critical alerts between 10:00 PM - 8:00 AM",
                    checked = settings.quietHoursEnabled,
                    onCheckedChange = onQuietHoursChange
                )

                HorizontalDivider(color = InboTheme.colors.divider, thickness = 1.dp)

                SettingsSwitchRow(
                    title = "Morning Digest",
                    subtitle = "Receive a single curated summary at 8:30 AM",
                    checked = settings.morningDigestEnabled,
                    onCheckedChange = onMorningDigestChange
                )

                HorizontalDivider(color = InboTheme.colors.divider, thickness = 1.dp)

                SettingsSwitchRow(
                    title = "Task Deadlines",
                    subtitle = "Remind 30 minutes before task due time",
                    checked = settings.taskRemindersEnabled,
                    onCheckedChange = onTaskRemindersChange
                )

                HorizontalDivider(color = InboTheme.colors.divider, thickness = 1.dp)

                SettingsSwitchRow(
                    title = "Haptic Feedback",
                    subtitle = "Subtle tactile tap when completing a task",
                    checked = settings.hapticFeedbackEnabled,
                    onCheckedChange = onHapticFeedbackChange
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // App Preferences Section
        Text(
            text = "App Behavior",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InboTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        InboCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsSwitchRow(
                    title = "Auto-archive Completed",
                    subtitle = "Automatically archive completed tasks after 24 hours",
                    checked = settings.autoArchiveEnabled,
                    onCheckedChange = onAutoArchiveChange
                )

                HorizontalDivider(color = InboTheme.colors.divider, thickness = 1.dp)

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
                            color = InboTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Screen displayed when opening Inbo",
                            fontSize = 12.sp,
                            color = InboTheme.colors.textMuted
                        )
                    }

                    InboBadge(
                        text = settings.defaultLaunchScreen,
                        containerColor = InboTheme.colors.softPurple,
                        contentColor = InboTheme.colors.accentPurple
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
                        color = InboTheme.colors.textPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(bottom = 5.dp, start = 2.dp)
                            .size(5.dp)
                            .background(InboTheme.colors.accentPurple, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "LESS NOISE. MORE LIFE.",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = InboTheme.colors.textMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Version 1.0.0",
                    fontSize = 12.sp,
                    color = InboTheme.colors.textMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Inbo is designed to protect your attention,\neliminating notification chaos with calm, focused surfaces.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = InboTheme.colors.textMuted,
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
                color = InboTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = InboTheme.colors.textMuted
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = InboTheme.colors.accentPurple,
                uncheckedThumbColor = InboTheme.colors.textSubtle,
                uncheckedTrackColor = InboTheme.colors.cardSurfaceSubtle
            )
        )
    }
}
