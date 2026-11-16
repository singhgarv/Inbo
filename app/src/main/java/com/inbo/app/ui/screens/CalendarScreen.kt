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

    val events = remember {
        mutableStateListOf(
            CalendarEvent(
                id = 1,
                title = "Design System & Compose Sync",
                timeRange = "09:30 AM - 10:15 AM",
                type = "Meeting",
                location = "Design Room / Virtual"
            ),
            CalendarEvent(
                id = 2,
                title = "Deep Work: Inbo Core Polish",
                timeRange = "11:00 AM - 12:30 PM",
                type = "Focus Time",
                location = "Do Not Disturb"
            ),
            CalendarEvent(
                id = 3,
                title = "Product Architecture Review",
                timeRange = "03:00 PM - 03:45 PM",
                type = "Review",
                location = "Conference Room B"
            )
        )
    }

    val deadlines = remember {
        mutableStateListOf(
            CalendarDeadline(
                id = 1,
                title = "Inbo Android UI Freeze",
                dueDate = "Tomorrow, 5:00 PM",
                tag = "High Priority",
                completed = false
            ),
            CalendarDeadline(
                id = 2,
                title = "Sprint Milestone Documentation",
                dueDate = "Thu, 12:00 PM",
                tag = "Milestone",
                completed = false
            ),
            CalendarDeadline(
                id = 3,
                title = "Publish Release Candidate",
                dueDate = "Friday, 6:00 PM",
                tag = "Release",
                completed = true
            )
        )
    }

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
            color = InboMuted
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
                        .background(if (isSelected) InboPurple else InboCardWhite)
                        .border(
                            1.dp,
                            if (isSelected) InboPurple else InboBorder,
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
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else InboMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = date,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else InboInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(if (isSelected) Color.White else InboPurple, CircleShape)
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
                color = InboInk
            )

            Text(
                text = "+ Add event",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = InboPurple,
                modifier = Modifier.clickable { showAddEventDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (events.isEmpty()) {
            InboCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No events scheduled for this day.",
                    fontSize = 13.sp,
                    color = InboMuted
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                events.forEach { event ->
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
                                color = InboPurple
                            )

                            InboBadge(
                                text = event.type,
                                containerColor = when (event.type) {
                                    "Focus Time" -> InboSoftPurple
                                    "Review" -> Color(0xFFF2EDFF)
                                    else -> InboLightPurple
                                },
                                contentColor = InboPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = event.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InboInk
                        )

                        if (event.location != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.location,
                                fontSize = 12.sp,
                                color = InboMuted
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
            color = InboInk
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            deadlines.forEachIndexed { index, deadline ->
                InboCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (deadline.completed) Color(0xFFFAFAFD) else InboCardWhite
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (deadline.completed) InboPurple else InboBackground)
                                .border(
                                    1.dp,
                                    if (deadline.completed) InboPurple else InboSubtle,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    deadlines[index] = deadline.copy(completed = !deadline.completed)
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
                                color = if (deadline.completed) InboMuted else InboInk
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = deadline.dueDate,
                                fontSize = 12.sp,
                                color = InboMuted
                            )
                        }

                        InboBadge(
                            text = deadline.tag,
                            containerColor = if (deadline.completed) InboBackground else InboSoftPurple,
                            contentColor = if (deadline.completed) InboMuted else InboPurple
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

        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = {
                Text("Schedule Event", fontWeight = FontWeight.Bold, color = InboInk)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Event title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newTime,
                        onValueChange = { newTime = it },
                        label = { Text("Time range") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.trim().isNotEmpty()) {
                            events.add(
                                CalendarEvent(
                                    id = events.size + 1,
                                    title = newTitle.trim(),
                                    timeRange = newTime.trim(),
                                    type = "Meeting"
                                )
                            )
                            showAddEventDialog = false
                        }
                    }
                ) {
                    Text("Add", color = InboPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("Cancel", color = InboMuted)
                }
            },
            containerColor = InboCardWhite,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
