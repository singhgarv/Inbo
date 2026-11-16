package com.inbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inbo.app.model.InboTask
import com.inbo.app.ui.components.*
import com.inbo.app.ui.theme.*

@Composable
fun TasksScreen(
    tasks: List<InboTask>,
    onAddTask: (title: String, category: String) -> Unit,
    onToggleTask: (id: Int, completed: Boolean) -> Unit,
    onDeleteTask: (id: Int) -> Unit,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var taskInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Work") }
    var selectedFilter by remember { mutableStateOf("All") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val total = tasks.size
    val remaining = tasks.count { !it.completed }
    val completed = total - remaining
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

    val filteredTasks = when (selectedFilter) {
        "Active" -> tasks.filter { !it.completed }
        "Completed" -> tasks.filter { it.completed }
        else -> tasks
    }

    val submitTask: () -> Unit = {
        val trimmed = taskInput.trim()
        if (trimmed.isNotEmpty()) {
            onAddTask(trimmed, selectedCategory)
            taskInput = ""
            keyboardController?.hide()
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
            eyebrow = "YOUR TASKS",
            title = "Make it happen.",
            subtitle = if (remaining == 0 && total > 0) {
                "All done! You have completed all tasks."
            } else {
                "$remaining ${if (remaining == 1) "task" else "tasks"} remaining"
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar for completed tasks
        if (total > 0) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$completed of $total completed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = InboTheme.colors.textMuted
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InboTheme.colors.accentPurple
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = InboTheme.colors.accentPurple,
                    trackColor = InboTheme.colors.softPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Add Task Card
        InboCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ADD A TASK",
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
                color = InboTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = taskInput,
                onValueChange = { taskInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("What needs to get done?", color = InboTheme.colors.textMuted, fontSize = 14.sp)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = InboTheme.colors.accentPurple,
                    unfocusedBorderColor = InboTheme.colors.cardBorder,
                    cursorColor = InboTheme.colors.accentPurple,
                    focusedTextColor = InboTheme.colors.textPrimary,
                    unfocusedTextColor = InboTheme.colors.textPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submitTask() })
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category tag selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Work", "Design", "Personal").forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) InboTheme.colors.softPurple else InboTheme.colors.cardSurfaceSubtle)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) InboTheme.colors.accentPurple else InboTheme.colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            InboButton(
                text = "Add task",
                onClick = submitTask,
                modifier = Modifier.fillMaxWidth(),
                icon = InboIcons.Add,
                enabled = taskInput.trim().isNotEmpty()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Filter chips & List Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Task List",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InboTheme.colors.textPrimary
            )

            if (completed > 0) {
                Text(
                    text = "Clear completed",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InboTheme.colors.accentPurple,
                    modifier = Modifier.clickable { onClearCompleted() }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "All" to total,
                "Active" to remaining,
                "Completed" to completed
            ).forEach { (filter, count) ->
                InboFilterChip(
                    text = "$filter ($count)",
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Task Items List
        if (filteredTasks.isEmpty()) {
            InboCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(InboTheme.colors.softPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✦", fontSize = 20.sp, color = InboTheme.colors.accentPurple)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (selectedFilter == "Completed") "No completed tasks yet" else "Nothing on your list",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = InboTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (selectedFilter == "Completed") "Check off items when you finish them." else "Add your first task above to get started.",
                        fontSize = 13.sp,
                        color = InboTheme.colors.textMuted
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredTasks.forEach { task ->
                    InboCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        containerColor = if (task.completed) InboTheme.colors.cardSurfaceSubtle else InboTheme.colors.cardSurface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.completed,
                                onCheckedChange = { isChecked ->
                                    onToggleTask(task.id, isChecked)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = InboTheme.colors.accentPurple,
                                    uncheckedColor = InboTheme.colors.textSubtle,
                                    checkmarkColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (task.completed) FontWeight.Normal else FontWeight.Medium,
                                    color = if (task.completed) InboTheme.colors.textMuted else InboTheme.colors.textPrimary,
                                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                InboBadge(
                                    text = task.category,
                                    containerColor = if (task.completed) InboTheme.colors.cardSurfaceSubtle else InboTheme.colors.softPurple,
                                    contentColor = if (task.completed) InboTheme.colors.textMuted else InboTheme.colors.accentPurple
                                )
                            }

                            IconButton(
                                onClick = {
                                    onDeleteTask(task.id)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = InboIcons.Delete,
                                    contentDescription = "Delete task",
                                    tint = InboTheme.colors.textSubtle,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
