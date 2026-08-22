package com.example.ui.components

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
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.InsertChart
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.LimeGradientEnd
import com.example.ui.theme.LimeGradientStart
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnLime
import com.example.ui.theme.TextPrimary

enum class FitlitNavDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    PLAN("Plan", Icons.Rounded.DateRange),
    FRIDGE("Fridge", Icons.Rounded.Kitchen),
    TRACKING("Tracking", Icons.Rounded.InsertChart),
    PROFILE("Profile", Icons.Rounded.Person)
}

@Composable
fun FitlitBottomBar(
    currentDestination: String,
    onNavigate: (String) -> Unit,
    onQuickAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating glass bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(34.dp),
                    ambientColor = Color(0x14000000),
                    spotColor = Color(0x30A6E324)
                )
                .clip(RoundedCornerShape(34.dp))
                .background(Color(0xF0FFFFFF))
                .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(34.dp))
                .padding(horizontal = 14.dp)
        ) {
            Row(
                modifier = Modifier.matchParentSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Home
                NavTabItem(
                    label = "Home",
                    icon = Icons.Rounded.Home,
                    selected = currentDestination == "home",
                    onClick = { onNavigate("home") },
                    testTag = "nav_home"
                )

                // Plan
                NavTabItem(
                    label = "Plan",
                    icon = Icons.Rounded.DateRange,
                    selected = currentDestination == "plan",
                    onClick = { onNavigate("plan") },
                    testTag = "nav_plan"
                )

                // Spacer for Center Action Button
                Box(modifier = Modifier.size(52.dp))

                // Tracking
                NavTabItem(
                    label = "Tracking",
                    icon = Icons.Rounded.InsertChart,
                    selected = currentDestination == "tracking",
                    onClick = { onNavigate("tracking") },
                    testTag = "nav_tracking"
                )

                // Profile
                NavTabItem(
                    label = "Profile",
                    icon = Icons.Rounded.Person,
                    selected = currentDestination == "profile",
                    onClick = { onNavigate("profile") },
                    testTag = "nav_profile"
                )
            }
        }

        // Center Floating Action Button with Glowing Lime Gradient
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(56.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x20000000),
                    spotColor = Color(0x60A6E324)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(LimeGradientStart, LimeGradientEnd, LimePrimaryDark)
                    )
                )
                .border(2.dp, Color(0x99FFFFFF), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onQuickAddClick
                )
                .testTag("nav_quick_add"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Quick Log",
                tint = TextOnLime,
                modifier = Modifier.size(28.dp)
            )
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
    val iconColor = if (selected) LimePrimaryDark else TextMuted
    val textColor = if (selected) TextPrimary else TextMuted
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}
