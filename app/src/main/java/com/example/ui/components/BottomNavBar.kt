package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.InsertChart
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class FitlitNavDestination(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Rounded.Home),
    PLAN("plan", "Plan", Icons.Rounded.DateRange),
    TASKS("tasks", "Tasks", Icons.Rounded.CheckCircle),
    TRACKING("tracking", "Tracking", Icons.Rounded.InsertChart),
    PROFILE("profile", "Profile", Icons.Rounded.Person)
}

@Composable
fun FitlitBottomBar(
    currentDestination: String,
    onNavigate: (String) -> Unit,
    onQuickAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barBgColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    val barBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating glass bar container adaptive to current Theme
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(34.dp),
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = primaryColor.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(34.dp))
                .background(barBgColor)
                .border(1.2.dp, barBorderColor, RoundedCornerShape(34.dp))
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.matchParentSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 1. Home Tab
                NavTabItem(
                    label = "Home",
                    icon = Icons.Rounded.Home,
                    selected = currentDestination == "home",
                    onClick = { onNavigate("home") },
                    testTag = "nav_home"
                )

                // 2. Plan Tab
                NavTabItem(
                    label = "Plan",
                    icon = Icons.Rounded.DateRange,
                    selected = currentDestination == "plan",
                    onClick = { onNavigate("plan") },
                    testTag = "nav_plan"
                )

                // 3. Tasks / To-Do Tab
                NavTabItem(
                    label = "Tasks",
                    icon = Icons.Rounded.CheckCircle,
                    selected = currentDestination == "tasks",
                    onClick = { onNavigate("tasks") },
                    testTag = "nav_tasks"
                )

                // 4. Tracking Tab
                NavTabItem(
                    label = "Track",
                    icon = Icons.Rounded.InsertChart,
                    selected = currentDestination == "tracking",
                    onClick = { onNavigate("tracking") },
                    testTag = "nav_tracking"
                )
                
                // 5. Insight AI Tab
                NavTabItem(
                    label = "Insight",
                    icon = androidx.compose.material.icons.Icons.Rounded.Person,
                    selected = currentDestination == "insight",
                    onClick = { onNavigate("insight") },
                    testTag = "nav_insight"
                )

                // 6. Profile Tab
                NavTabItem(
                    label = "Profile",
                    icon = Icons.Rounded.Person,
                    selected = currentDestination == "profile",
                    onClick = { onNavigate("profile") },
                    testTag = "nav_profile"
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    val pillBgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)

    val iconTint by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        label = "tabIconTint"
    )
    val textTint by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        label = "tabTextTint"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tabScale"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) pillBgColor else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textTint
        )
    }
}
