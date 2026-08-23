package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.FatRose
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.ProteinBlue
import com.example.ui.theme.StepsGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnLime

enum class ResetActionType(
    val title: String,
    val description: String,
    val confirmMessage: String,
    val isDestructive: Boolean
) {
    CLEAR_DEMO_TRACKING(
        title = "Clear Demo & Live Tracking Data",
        description = "Removes seeded demo logs, today's meals, water, and sets live steps to 0. Starts clean real-time tracking while keeping your goal targets.",
        confirmMessage = "Are you sure you want to clear all demo activity and start fresh real-time tracking from 0?",
        isDestructive = false
    ),
    CLEAR_TODAY(
        title = "Clear Today's Logs Only",
        description = "Resets today's eaten meals, water intake, and active step counter to 0 for a fresh start today.",
        confirmMessage = "Are you sure you want to zero out today's logged meals and water intake?",
        isDestructive = false
    ),
    CLEAR_FRIDGE(
        title = "Empty Smart Fridge Inventory",
        description = "Removes all ingredients from your fridge inventory so you can scan or type your own pantry items.",
        confirmMessage = "Are you sure you want to remove all ingredients from your smart fridge?",
        isDestructive = false
    ),
    FACTORY_RESET(
        title = "Full Factory Reset & Restart",
        description = "Permanently clears all database logs, custom profile details, and returns to the initial welcome screen.",
        confirmMessage = "WARNING: This will permanently wipe all profile goals, meal plans, and logs, returning to the onboarding screen. Proceed?",
        isDestructive = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetDataBottomSheet(
    sheetState: SheetState,
    onResetDemoData: () -> Unit,
    onClearTodayLogs: () -> Unit,
    onClearFridge: () -> Unit,
    onFactoryReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var pendingAction by remember { mutableStateOf<ResetActionType?>(null) }

    // Confirmation Alert Dialog
    pendingAction?.let { action ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            icon = {
                Icon(
                    imageVector = if (action.isDestructive) Icons.Rounded.DeleteForever else Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = if (action.isDestructive) FatRose else CalorieOrange,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = action.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = action.confirmMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toExecute = action
                        pendingAction = null
                        when (toExecute) {
                            ResetActionType.CLEAR_DEMO_TRACKING -> {
                                onResetDemoData()
                                onDismiss()
                            }
                            ResetActionType.CLEAR_TODAY -> {
                                onClearTodayLogs()
                                onDismiss()
                            }
                            ResetActionType.CLEAR_FRIDGE -> {
                                onClearFridge()
                                onDismiss()
                            }
                            ResetActionType.FACTORY_RESET -> {
                                onFactoryReset()
                                onDismiss()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (action.isDestructive) FatRose else LimePrimary,
                        contentColor = if (action.isDestructive) Color.White else TextOnLime
                    ),
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text(
                        text = if (action.isDestructive) "Yes, Reset Everything" else "Confirm Reset",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline)
                    .align(Alignment.CenterHorizontally)
            )

            // Title and description
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Reset Data & Live Tracking",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Clear old demo data in real-time or start fresh. The app will immediately track your own real-time logs and pedometer sensor data.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. Recommended: Clear Demo & Tracking Data
                ResetOptionTile(
                    icon = Icons.Rounded.CleaningServices,
                    iconColor = LimePrimaryDark,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    badge = "RECOMMENDED",
                    badgeColor = LimePrimaryDark,
                    badgeBg = MaterialTheme.colorScheme.primaryContainer,
                    title = ResetActionType.CLEAR_DEMO_TRACKING.title,
                    description = ResetActionType.CLEAR_DEMO_TRACKING.description,
                    onClick = { pendingAction = ResetActionType.CLEAR_DEMO_TRACKING },
                    testTag = "reset_option_clear_demo"
                )

                // 2. Clear Today's Logs Only
                ResetOptionTile(
                    icon = Icons.Rounded.History,
                    iconColor = ProteinBlue,
                    iconBg = Color(0xFFE0F2FE),
                    badge = null,
                    badgeColor = Color.Unspecified,
                    badgeBg = Color.Unspecified,
                    title = ResetActionType.CLEAR_TODAY.title,
                    description = ResetActionType.CLEAR_TODAY.description,
                    onClick = { pendingAction = ResetActionType.CLEAR_TODAY },
                    testTag = "reset_option_clear_today"
                )

                // 3. Clear Smart Fridge Inventory
                ResetOptionTile(
                    icon = Icons.Rounded.Kitchen,
                    iconColor = StepsGreen,
                    iconBg = Color(0xFFDCFCE7),
                    badge = null,
                    badgeColor = Color.Unspecified,
                    badgeBg = Color.Unspecified,
                    title = ResetActionType.CLEAR_FRIDGE.title,
                    description = ResetActionType.CLEAR_FRIDGE.description,
                    onClick = { pendingAction = ResetActionType.CLEAR_FRIDGE },
                    testTag = "reset_option_clear_fridge"
                )

                // 4. Full Factory Reset
                ResetOptionTile(
                    icon = Icons.Rounded.DeleteForever,
                    iconColor = FatRose,
                    iconBg = Color(0xFFFFE4E6),
                    badge = "DANGER",
                    badgeColor = FatRose,
                    badgeBg = Color(0xFFFFE4E6),
                    title = ResetActionType.FACTORY_RESET.title,
                    description = ResetActionType.FACTORY_RESET.description,
                    onClick = { pendingAction = ResetActionType.FACTORY_RESET },
                    testTag = "reset_option_factory_reset"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResetOptionTile(
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    badge: String?,
    badgeColor: Color,
    badgeBg: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeColor
                            )
                        }
                    }
                }

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
