package com.inbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.inbo.app.model.CalendarDeadline
import com.inbo.app.model.CalendarEvent
import com.inbo.app.ui.components.*
import com.inbo.app.ui.theme.*

@Composable
fun CalendarScreen(
    events: List<CalendarEvent>,
    deadlines: List<CalendarDeadline>,
    onAddEvent: (title: String, timeRange: String, type: String, location: String?, date: String) -> Unit,
    onToggleDeadline: (id: Int, completed: Boolean) -> Unit,
    onDeleteEvent: (id: Int) -> Unit = {},
    onDeleteDeadline: (id: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    val daysOfWeek = listOf(
        Triple("Mon", "16", true),
        Triple("Tue", "17", false),
        Triple("Wed", "18", false),
        Triple("Thu", "19", false),
        Triple("Fri", "20", false),
        Triple("Sat", "21", false),
        Triple("Sun", "22", false)
    )

    val selectedDate = daysOfWeek[selectedDayIndex].second

    // Filter events by selected date
    val filteredEvents = events.filter { it.date == selectedDate }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        InboTopBar()

        Spacer(modifier = Modifier.height(28.dp))

        InboScreenHeader(
            eyebrow = "CALENDAR",
            title = "Focus on what's next.",
            subtitle = "Your scheduled commitments and key deadlines."
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Week strip selector
        Text(
            text = "NOVEMBER 2026",
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold,
            color = InboTheme.colors.textMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEachIndexed { index, (day, date, isToday) ->
                val isSelected = selectedDayIndex == index
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) InboTheme.colors.accentPurple else InboTheme.colors.cardSurface)
                        .border(
                            1.dp,
                            if (isSelected) InboTheme.colors.accentPurple else InboTheme.colors.cardBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedDayIndex = index }
                        .padding(vertical = 10.dp, horizontal = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else InboTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = date,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else InboTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(if (isSelected) Color.White else InboTheme.colors.accentPurple, CircleShape)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Today's schedule section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scheduled Events",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InboTheme.colors.textPrimary
            )

            Text(
                text = "+ Add event",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = InboTheme.colors.accentPurple,
                modifier = Modifier.clickable { showAddEventDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredEvents.isEmpty()) {
            InboCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No events scheduled for Nov $selectedDate.",
                    fontSize = 13.sp,
                    color = InboTheme.colors.textMuted
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredEvents.forEach { event ->
                    InboCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.timeRange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InboTheme.colors.accentPurple
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InboBadge(
                                    text = event.type,
                                    containerColor = when (event.type) {
                                        "Focus Time" -> InboTheme.colors.softPurple
                                        "Review" -> InboTheme.colors.cardSurfaceSubtle
                                        else -> InboTheme.colors.cardSurfaceSubtle
                                    },
                                    contentColor = InboTheme.colors.accentPurple
                                )

                                IconButton(
                                    onClick = { onDeleteEvent(event.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = InboIcons.Delete,
                                        contentDescription = "Delete event",
                                        tint = InboTheme.colors.textSubtle,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = event.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InboTheme.colors.textPrimary
                        )

                        if (event.location != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.location,
                                fontSize = 12.sp,
                                color = InboTheme.colors.textMuted
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Upcoming Deadlines section
        Text(
            text = "Upcoming Deadlines",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InboTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            deadlines.forEach { deadline ->
                InboCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (deadline.completed) InboTheme.colors.cardSurfaceSubtle else InboTheme.colors.cardSurface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (deadline.completed) InboTheme.colors.accentPurple else InboTheme.colors.cardSurfaceSubtle)
                                .border(
                                    1.dp,
                                    if (deadline.completed) InboTheme.colors.accentPurple else InboTheme.colors.textSubtle,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    onToggleDeadline(deadline.id, !deadline.completed)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (deadline.completed) {
                                Icon(
                                    imageVector = InboIcons.Check,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = deadline.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (deadline.completed) InboTheme.colors.textMuted else InboTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = deadline.dueDate,
                                fontSize = 12.sp,
                                color = InboTheme.colors.textMuted
                            )
                        }

                        InboBadge(
                            text = deadline.tag,
                            containerColor = if (deadline.completed) InboTheme.colors.cardSurfaceSubtle else InboTheme.colors.softPurple,
                            contentColor = if (deadline.completed) InboTheme.colors.textMuted else InboTheme.colors.accentPurple
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showAddEventDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newTime by remember { mutableStateOf("04:00 PM - 05:00 PM") }
        var selectedType by remember { mutableStateOf("Meeting") }

        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = {
                Text("Schedule Event", fontWeight = FontWeight.Bold, color = InboTheme.colors.textPrimary)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Event title", color = InboTheme.colors.textMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InboTheme.colors.accentPurple,
                            unfocusedBorderColor = InboTheme.colors.cardBorder,
                            cursorColor = InboTheme.colors.accentPurple,
                            focusedTextColor = InboTheme.colors.textPrimary,
                            unfocusedTextColor = InboTheme.colors.textPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newTime,
                        onValueChange = { newTime = it },
                        label = { Text("Time range", color = InboTheme.colors.textMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InboTheme.colors.accentPurple,
                            unfocusedBorderColor = InboTheme.colors.cardBorder,
                            cursorColor = InboTheme.colors.accentPurple,
                            focusedTextColor = InboTheme.colors.textPrimary,
                            unfocusedTextColor = InboTheme.colors.textPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InboTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Meeting", "Focus Time", "Review").forEach { type ->
                            val isChosen = selectedType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) InboTheme.colors.softPurple else InboTheme.colors.cardSurfaceSubtle)
                                    .clickable { selectedType = type }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 11.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) InboTheme.colors.accentPurple else InboTheme.colors.textMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adding to: Nov $selectedDate",
                        fontSize = 11.sp,
                        color = InboTheme.colors.accentPurple,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.trim().isNotEmpty()) {
                            onAddEvent(
                                newTitle.trim(),
                                newTime.trim(),
                                selectedType,
                                null,
                                selectedDate
                            )
                            showAddEventDialog = false
                        }
                    }
                ) {
                    Text("Add", color = InboTheme.colors.accentPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("Cancel", color = InboTheme.colors.textMuted)
                }
            },
            containerColor = InboTheme.colors.cardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
