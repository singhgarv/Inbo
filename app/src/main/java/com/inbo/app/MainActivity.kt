package com.inbo.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.inbo.app.data.local.CalendarDao
import com.inbo.app.data.local.InboDatabaseHelper
import com.inbo.app.data.local.NotificationDao
import com.inbo.app.data.local.SettingsDao
import com.inbo.app.data.local.TaskDao
import com.inbo.app.data.repository.CalendarRepository
import com.inbo.app.data.repository.NotificationRepository
import com.inbo.app.data.repository.SettingsRepository
import com.inbo.app.data.repository.TaskRepository
import com.inbo.app.model.InboTask
import com.inbo.app.notification.NotificationPermissionHelper
import com.inbo.app.ui.components.InboIcons
import com.inbo.app.ui.screens.CalendarScreen
import com.inbo.app.ui.screens.InboxScreen
import com.inbo.app.ui.screens.SettingsScreen
import com.inbo.app.ui.screens.TasksScreen
import com.inbo.app.ui.theme.*

class MainActivity : ComponentActivity() {
    private lateinit var taskRepository: TaskRepository
    private lateinit var calendarRepository: CalendarRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var notificationRepository: NotificationRepository

    private val isNotificationAccessGrantedState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize persistent SQLite data layer
        val dbHelper = InboDatabaseHelper(applicationContext)
        val taskDao = TaskDao(dbHelper)
        val calendarDao = CalendarDao(dbHelper)
        val settingsDao = SettingsDao(dbHelper)
        val notificationDao = NotificationDao(dbHelper)

        taskRepository = TaskRepository(taskDao, lifecycleScope)
        calendarRepository = CalendarRepository(calendarDao, lifecycleScope)
        settingsRepository = SettingsRepository(settingsDao, lifecycleScope)
        notificationRepository = NotificationRepository(notificationDao, lifecycleScope)

        // Set singleton instance for InboNotificationListenerService
        NotificationRepository.setInstance(notificationRepository)

        updateNotificationPermissionState()

        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle()
            InboTheme(darkTheme = settings.darkModeEnabled) {
                InboApp(
                    taskRepository = taskRepository,
                    calendarRepository = calendarRepository,
                    settingsRepository = settingsRepository,
                    notificationRepository = notificationRepository,
                    isNotificationAccessGranted = isNotificationAccessGrantedState.value,
                    onOpenNotificationAccessSettings = {
                        try {
                            startActivity(NotificationPermissionHelper.createNotificationAccessSettingsIntent())
                        } catch (_: Exception) {
                            // Fallback gracefully if intent is unresolvable on custom ROMs
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh notification listener status whenever user returns from Settings
        updateNotificationPermissionState()
    }

    private fun updateNotificationPermissionState() {
        isNotificationAccessGrantedState.value = NotificationPermissionHelper.isNotificationAccessGranted(this)
    }
}

@Composable
fun InboApp(
    taskRepository: TaskRepository,
    calendarRepository: CalendarRepository,
    settingsRepository: SettingsRepository,
    notificationRepository: NotificationRepository,
    isNotificationAccessGranted: Boolean,
    onOpenNotificationAccessSettings: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Collect reactive persistent tasks from repository
    val tasks by taskRepository.tasks.collectAsStateWithLifecycle()

    // Collect reactive persistent calendar events and deadlines from repository
    val events by calendarRepository.events.collectAsStateWithLifecycle()
    val deadlines by calendarRepository.deadlines.collectAsStateWithLifecycle()

    // Collect reactive persistent settings from repository
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()

    // Collect reactive persistent WhatsApp notifications from repository
    val notifications by notificationRepository.notifications.collectAsStateWithLifecycle()

    val tabs = listOf("Inbox", "Tasks", "Calendar", "Settings")
    val tabIcons = listOf(
        InboIcons.Inbox,
        InboIcons.Tasks,
        InboIcons.Calendar,
        InboIcons.Settings
    )

    val remainingTasksCount = tasks.count { !it.completed }

    // Independent scroll states per screen for fluid user experience
    val inboxScrollState = rememberScrollState()
    val tasksScrollState = rememberScrollState()
    val calendarScrollState = rememberScrollState()
    val settingsScrollState = rememberScrollState()

    Scaffold(
        containerColor = InboTheme.colors.background,
        bottomBar = {
            Column {
                HorizontalDivider(color = InboTheme.colors.divider, thickness = 1.dp)
                NavigationBar(
                    containerColor = InboTheme.colors.cardSurface,
                    contentColor = InboTheme.colors.accentPurple,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = tabIcons[index],
                                    contentDescription = tab,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = InboTheme.colors.accentPurple,
                                selectedTextColor = InboTheme.colors.accentPurple,
                                indicatorColor = InboTheme.colors.softPurple,
                                unselectedIconColor = InboTheme.colors.textMuted,
                                unselectedTextColor = InboTheme.colors.textMuted
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(InboTheme.colors.background)
        ) {
            when (selectedTab) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(inboxScrollState)
                    ) {
                        InboxScreen(
                            notifications = notifications,
                            remainingTasksCount = remainingTasksCount,
                            onMarkAsRead = { id ->
                                notificationRepository.markNotificationAsRead(id, true)
                            },
                            onMarkAllAsRead = {
                                notificationRepository.markAllNotificationsAsRead()
                            },
                            onNavigateToTasks = { selectedTab = 1 }
                        )
                    }
                }
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(tasksScrollState)
                    ) {
                        TasksScreen(
                            tasks = tasks,
                            onAddTask = { title, category ->
                                taskRepository.addTask(title, category)
                            },
                            onToggleTask = { id, completed ->
                                taskRepository.toggleTaskCompletion(id, completed)
                            },
                            onDeleteTask = { id ->
                                taskRepository.deleteTask(id)
                            },
                            onClearCompleted = {
                                taskRepository.clearCompletedTasks()
                            }
                        )
                    }
                }
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(calendarScrollState)
                    ) {
                        CalendarScreen(
                            events = events,
                            deadlines = deadlines,
                            onAddEvent = { title, timeRange, type, location, date ->
                                calendarRepository.addEvent(title, timeRange, type, location, date)
                            },
                            onToggleDeadline = { id, completed ->
                                calendarRepository.toggleDeadlineCompletion(id, completed)
                            },
                            onDeleteEvent = { id ->
                                calendarRepository.deleteEvent(id)
                            },
                            onDeleteDeadline = { id ->
                                calendarRepository.deleteDeadline(id)
                            }
                        )
                    }
                }
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(settingsScrollState)
                    ) {
                        SettingsScreen(
                            settings = settings,
                            isNotificationAccessGranted = isNotificationAccessGranted,
                            onOpenNotificationAccessSettings = onOpenNotificationAccessSettings,
                            onDarkModeChange = { settingsRepository.setDarkModeEnabled(it) },
                            onQuietHoursChange = { settingsRepository.setQuietHoursEnabled(it) },
                            onMorningDigestChange = { settingsRepository.setMorningDigestEnabled(it) },
                            onTaskRemindersChange = { settingsRepository.setTaskRemindersEnabled(it) },
                            onHapticFeedbackChange = { settingsRepository.setHapticFeedbackEnabled(it) },
                            onAutoArchiveChange = { settingsRepository.setAutoArchiveEnabled(it) }
                        )
                    }
                }
            }
        }
    }
}
