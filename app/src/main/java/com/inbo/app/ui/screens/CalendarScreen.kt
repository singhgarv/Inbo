package com.inbo.app.ui.screens

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private data class CalendarDayItem(
    val dayOfWeekLabel: String,
    val dayOfMonthText: String,
    val isoDate: String,
    val isToday: Boolean
)

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
    var today by remember { mutableStateOf(LocalDate.now()) }
    val todayIso = remember(today) { today.toString() }

    // Selected date defaults to today whenever Calendar opens
    var selectedIsoDate by remember { mutableStateOf(todayIso) }

    // Start of currently viewed week (Monday)
    var currentWeekMonday by remember {
        mutableStateOf(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    }

    var showAddEventDialog by remember { mutableStateOf(false) }

    // Refresh today's date whenever Inbo returns to the foreground.
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val newToday = LocalDate.now()
                val previousToday = today

                if (newToday != previousToday) {
                    val previousTodayIso = previousToday.toString()
                    val previousMonday = previousToday.with(
                        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                    )

                    val wasViewingToday =
                        currentWeekMonday == previousMonday &&
                                selectedIsoDate == previousTodayIso

                    today = newToday

                    if (wasViewingToday) {
                        currentWeekMonday = newToday.with(
                            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                        )
                        selectedIsoDate = newToday.toString()
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val daysOfWeek: List<CalendarDayItem> = remember(currentWeekMonday, todayIso) {
        val shortDayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.US)
        (0L..6L).map { offset ->
            val date = currentWeekMonday.plusDays(offset)
            val iso = date.toString()
            CalendarDayItem(
                dayOfWeekLabel = date.format(shortDayFormatter),
                dayOfMonthText = date.dayOfMonth.toString(),
                isoDate = iso,
                isToday = iso == todayIso
            )
        }
    }

    // Dynamic month and year label reflecting the displayed week
    val monthYearHeader = remember(currentWeekMonday) {
        val weekEnd = currentWeekMonday.plusDays(6)
        val monthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.US)
        if (currentWeekMonday.month == weekEnd.month && currentWeekMonday.year == weekEnd.year) {
            "${currentWeekMonday.format(monthFormatter).uppercase(Locale.US)} ${currentWeekMonday.year}"
        } else if (currentWeekMonday.year == weekEnd.year) {
            "${currentWeekMonday.format(monthFormatter).uppercase(Locale.US)} / ${weekEnd.format(monthFormatter).uppercase(Locale.US)} ${currentWeekMonday.year}"
        } else {
            "${currentWeekMonday.format(monthFormatter).uppercase(Locale.US)} ${currentWeekMonday.year} / ${weekEnd.format(monthFormatter).uppercase(Locale.US)} ${weekEnd.year}"
        }
    }

    // Friendly date header for the scheduled events card
    val selectedDateFriendly = remember(selectedIsoDate) {
        try {
            val parsed = LocalDate.parse(selectedIsoDate)
            parsed.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))
        } catch (_: Exception) {
            selectedIsoDate
        }
    }

    // Filter events by selected date:
    // Matches exact ISO date (e.g. "2026-11-17").
    // For legacy records where only day-of-month was stored (e.g. "17"), safely matches when
    // the day number coincides with the selected date's day of month without fabricating months.
    val filteredEvents = events.filter {
        if (it.date == selectedIsoDate) {
            true
        } else if (it.date.length <= 2) {
            val selectedDayStr = try {
                LocalDate.parse(selectedIsoDate).dayOfMonth.toString()
            } catch (_: Exception) {
                ""
            }
            it.date.trimStart('0') == selectedDayStr.trimStart('0')
        } else {
            false
        }
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

        // Week strip selector with previous/next week navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthYearHeader,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                color = InboTheme.colors.textMuted
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        val prevMonday = currentWeekMonday.minusWeeks(1)
                        currentWeekMonday = prevMonday
                        selectedIsoDate = prevMonday.toString()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = InboIcons.ChevronLeft,
                        contentDescription = "Previous week",
                        tint = InboTheme.colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val nextMonday = currentWeekMonday.plusWeeks(1)
                        currentWeekMonday = nextMonday
                        selectedIsoDate = nextMonday.toString()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = InboIcons.ChevronRight,
                        contentDescription = "Next week",
                        tint = InboTheme.colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { dayItem ->
                val isSelected = selectedIsoDate == dayItem.isoDate
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) InboTheme.colors.accentPurple else InboTheme.colors.cardSurface)
                        .border(
                            1.dp,
                            if (isSelected) InboTheme.colors.accentPurple else InboTheme.colors.cardBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedIsoDate = dayItem.isoDate }
                        .padding(vertical = 10.dp, horizontal = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayItem.dayOfWeekLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else InboTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayItem.dayOfMonthText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else InboTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (dayItem.isToday) {
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

        // Schedule section
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
                    text = "No events scheduled for $selectedDateFriendly.",
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
                        text = "Adding to: $selectedDateFriendly ($selectedIsoDate)",
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
                                selectedIsoDate
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
