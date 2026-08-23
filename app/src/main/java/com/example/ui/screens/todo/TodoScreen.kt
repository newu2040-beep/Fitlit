package com.example.ui.screens.todo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TodoEntity
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.ProteinBlue
import com.example.ui.theme.StepsGreen
import com.example.util.ApiKeyStatus
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TodoScreen(
    todos: List<TodoEntity>,
    isGeneratingSchedule: Boolean,
    geminiKeyStatus: ApiKeyStatus,
    onAddTodoClick: () -> Unit,
    onEditTodoClick: (TodoEntity) -> Unit,
    onToggleCompleted: (TodoEntity) -> Unit,
    onDeleteTodo: (Long) -> Unit,
    onGenerateAiSchedule: () -> Unit,
    onManageApiKeyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var currentEpochMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Live clock ticker for live minute precision
    LaunchedEffect(Unit) {
        while (true) {
            currentEpochMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val now = LocalDateTime.now()
    val dateDisplay = now.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
    val timeDisplay = now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"))
    val todayStr = LocalDate.now().toString()

    val filteredTodos = remember(todos, selectedFilter) {
        when (selectedFilter) {
            "All" -> todos
            "Today" -> todos.filter { it.dueDateStr == todayStr }
            "Workouts" -> todos.filter { it.category.equals("Workout", ignoreCase = true) }
            "Nutrition" -> todos.filter { it.category.equals("Nutrition", ignoreCase = true) }
            "Hydration" -> todos.filter { it.category.equals("Hydration", ignoreCase = true) }
            "Habits" -> todos.filter { it.category.equals("Habit", ignoreCase = true) || it.category.equals("Supplement", ignoreCase = true) }
            "Completed" -> todos.filter { it.isCompleted }
            "Pending" -> todos.filter { !it.isCompleted }
            else -> todos
        }
    }

    val totalCount = todos.size
    val completedCount = todos.count { it.isCompleted }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            // Header: Live Date & Time with Precision Seconds & Minutes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateDisplay,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Daily Schedule",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = timeDisplay,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onManageApiKeyClick,
                    modifier = Modifier.testTag("todo_api_key_btn")
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = "Gemini Key",
                            tint = if (geminiKeyStatus == ApiKeyStatus.QUOTA_EXCEEDED) CalorieOrange else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Quota Exceeded / Missing Key Warning Banner
            if (geminiKeyStatus == ApiKeyStatus.QUOTA_EXCEEDED || geminiKeyStatus == ApiKeyStatus.MISSING) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CalorieOrange.copy(alpha = 0.12f))
                        .border(1.dp, CalorieOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onManageApiKeyClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = null,
                            tint = CalorieOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (geminiKeyStatus == ApiKeyStatus.QUOTA_EXCEEDED) "Gemini Quota Exceeded • Tap to add API Key" else "Add your Gemini API Key for smart schedules",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CalorieOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Schedule Overview & Progress Summary Card
                item {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("todo_summary_card"),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        borderColor = MaterialTheme.colorScheme.outline
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Tasks & Goals Completion",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$completedCount of $totalCount completed (${(progressFraction * 100).toInt()}%)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (progressFraction >= 1f) StepsGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${(progressFraction * 100).toInt()}% Done",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (progressFraction >= 1f) StepsGreen else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // AI Schedule Generator Action Card Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onGenerateAiSchedule,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("generate_ai_schedule_btn"),
                                    enabled = !isGeneratingSchedule,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    if (isGeneratingSchedule) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("AI Structuring...", fontSize = 12.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("AI Optimize Schedule", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = onAddTodoClick,
                                    modifier = Modifier.testTag("add_custom_task_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Filter Tabs Carousel
                item {
                    val filterOptions = listOf("All", "Today", "Workouts", "Nutrition", "Hydration", "Habits", "Pending", "Completed")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterOptions.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Task List Items
                if (filteredTodos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No tasks found in $selectedFilter",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap 'Add' or 'AI Optimize Schedule' to create tasks",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTodos, key = { it.id }) { todo ->
                        TodoCardItem(
                            todo = todo,
                            currentEpochMillis = currentEpochMillis,
                            onToggle = { onToggleCompleted(todo) },
                            onEdit = { onEditTodoClick(todo) },
                            onDelete = { onDeleteTodo(todo.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
}

@Composable
private fun TodoCardItem(
    todo: TodoEntity,
    currentEpochMillis: Long,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDone = todo.isCompleted

    val (catIcon, catColor) = when (todo.category.lowercase()) {
        "workout" -> Icons.Rounded.FitnessCenter to StepsGreen
        "nutrition" -> Icons.Rounded.LocalDining to CalorieOrange
        "hydration" -> Icons.Rounded.WaterDrop to ProteinBlue
        "supplement" -> Icons.Rounded.Medication to Color(0xFFA855F7)
        "habit" -> Icons.Rounded.Nightlight to Color(0xFF6366F1)
        else -> Icons.Rounded.Category to Color(0xFF64748B)
    }

    // Precise Minute calculation
    val minuteStatusText = remember(todo.dueTimestamp, todo.isCompleted, currentEpochMillis) {
        if (todo.isCompleted) {
            "Completed ✓"
        } else if (todo.dueTimestamp > 0) {
            val diffMillis = todo.dueTimestamp - currentEpochMillis
            val diffMins = (diffMillis / 60000L).toInt()
            when {
                diffMins > 60 -> "Due in ${diffMins / 60}h ${diffMins % 60}m"
                diffMins in 1..60 -> "Due in $diffMins mins"
                diffMins == 0 -> "Due right now"
                diffMins in -60..-1 -> "${-diffMins} mins overdue"
                else -> "${-diffMins / 60}h overdue"
            }
        } else {
            "Scheduled"
        }
    }

    val minuteColor = when {
        isDone -> StepsGreen
        minuteStatusText.contains("overdue") -> Color(0xFFEF4444)
        minuteStatusText.contains("Due in") && minuteStatusText.contains("mins") -> CalorieOrange
        else -> MaterialTheme.colorScheme.primary
    }

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("todo_item_${todo.id}"),
        backgroundColor = if (isDone) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
        borderColor = if (isDone) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Custom Animated Checkbox
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isDone) StepsGreen else Color.Transparent)
                        .border(
                            2.dp,
                            if (isDone) StepsGreen else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                        .clickable(onClick = onToggle)
                        .testTag("todo_checkbox_${todo.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Title & Badges
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todo.title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                    )

                    if (todo.description.isNotBlank()) {
                        Text(
                            text = todo.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                // Actions: Edit & Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Bottom Badges: Category, Priority, Exact Time & Minute Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(catColor.copy(alpha = 0.14f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = catIcon,
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = todo.category,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = catColor
                            )
                        }
                    }

                    // Priority Chip
                    val pColor = when (todo.priority) {
                        "High" -> Color(0xFFEF4444)
                        "Medium" -> CalorieOrange
                        else -> StepsGreen
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(pColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = todo.priority,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = pColor
                        )
                    }

                    if (todo.isAiGenerated) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "✨ AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Exact Time & Minute Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = minuteColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${todo.dueTimeStr} • $minuteStatusText",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = minuteColor
                    )
                }
            }
        }
    }
}
