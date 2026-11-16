package com.inbo.app

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inbo.app.model.InboTask
import com.inbo.app.ui.components.InboIcons
import com.inbo.app.ui.screens.CalendarScreen
import com.inbo.app.ui.screens.InboxScreen
import com.inbo.app.ui.screens.SettingsScreen
import com.inbo.app.ui.screens.TasksScreen
import com.inbo.app.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InboTheme {
                InboApp()
            }
        }
    }
}

@Composable
fun InboApp() {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Pre-populate with realistic, calm starter tasks preserving full functional task management
    val tasks = remember {
        mutableStateListOf(
            InboTask(
                id = 1,
                title = "Review Q4 product roadmap & goals",
                completed = false,
                category = "Work"
            ),
            InboTask(
                id = 2,
                title = "Finalize Inbo Android UI design tokens",
                completed = false,
                category = "Design"
            ),
            InboTask(
                id = 3,
                title = "Clean up project structure and remove noise",
                completed = true,
                category = "Work"
            )
        )
    }
    var nextTaskId by remember { mutableIntStateOf(4) }

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
        containerColor = InboBackground,
        bottomBar = {
            Column {
                HorizontalDivider(color = InboDivider, thickness = 1.dp)
                NavigationBar(
                    containerColor = InboCardWhite,
                    contentColor = InboPurple,
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
                                selectedIconColor = InboPurple,
                                selectedTextColor = InboPurple,
                                indicatorColor = InboSoftPurple,
                                unselectedIconColor = InboMuted,
                                unselectedTextColor = InboMuted
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
                .background(InboBackground)
        ) {
            when (selectedTab) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(inboxScrollState)
                    ) {
                        InboxScreen(
                            remainingTasksCount = remainingTasksCount,
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
                            nextId = nextTaskId,
                            onNextIdIncrement = { nextTaskId++ }
                        )
                    }
                }
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(calendarScrollState)
                    ) {
                        CalendarScreen()
                    }
                }
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(settingsScrollState)
                    ) {
                        SettingsScreen()
                    }
                }
            }
        }
    }
}

