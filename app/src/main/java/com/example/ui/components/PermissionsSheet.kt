package com.example.ui.components

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.StepsGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPermissionsUpdated: () -> Unit = {}
) {
    val context = LocalContext.current
    var refreshTrigger by remember { mutableStateOf(0) }

    val isCameraGranted = remember(refreshTrigger) { PermissionUtils.isCameraGranted(context) }
    val isNotificationGranted = remember(refreshTrigger) { PermissionUtils.isNotificationGranted(context) }
    val isGalleryGranted = remember(refreshTrigger) { PermissionUtils.isGalleryGranted(context) }
    val isActivityGranted = remember(refreshTrigger) { PermissionUtils.isActivityRecognitionGranted(context) }

    val allGranted = isCameraGranted && isNotificationGranted && isGalleryGranted && isActivityGranted

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshTrigger++
        onPermissionsUpdated()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0FDF4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = LimePrimaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "App Permissions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = if (allGranted) "All Allowed ✓" else "Full Access",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (allGranted) StepsGreen else LimePrimaryDark
                )
            }

            Text(
                text = "Allowing permissions enables Fitlit to scan your fridge ingredients, analyze meal plate photos, deliver timely meal reminders, and sync step counts accurately.",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PermissionItemCard(
                    icon = Icons.Rounded.CameraAlt,
                    title = "Camera Access",
                    description = "Take live photos of your fridge shelves and meal plates",
                    isGranted = isCameraGranted,
                    onRequestClick = {
                        multiplePermissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    }
                )

                PermissionItemCard(
                    icon = Icons.Rounded.PhotoLibrary,
                    title = "Photo Gallery & Media",
                    description = "Select meal photos & grocery labels from your gallery",
                    isGranted = isGalleryGranted,
                    onRequestClick = {
                        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        multiplePermissionsLauncher.launch(perms)
                    }
                )

                PermissionItemCard(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Smart Notifications",
                    description = "Get timely meal prep notifications & streak milestones",
                    isGranted = isNotificationGranted,
                    onRequestClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            multiplePermissionsLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                        }
                    }
                )

                PermissionItemCard(
                    icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                    title = "Activity & Fitness Recognition",
                    description = "Track daily step counts and active calorie burn accurately",
                    isGranted = isActivityGranted,
                    onRequestClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            multiplePermissionsLauncher.launch(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Action Button: "Allow All Permissions" or "Settings"
            if (!allGranted) {
                LiquidGlassButton(
                    onClick = {
                        multiplePermissionsLauncher.launch(PermissionUtils.getRequiredPermissions())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("allow_all_permissions_btn")
                ) {
                    Text(
                        text = "Allow All Permissions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextOnLime
                    )
                }
            } else {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LimePrimaryDark),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(
                        text = "All Permissions Active ✓",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // System settings secondary link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { PermissionUtils.openAppSettings(context) }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage in Android App Settings",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequestClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (isGranted) Color(0xFFF9FDF5) else Color.White,
        borderColor = if (isGranted) Color(0xFFD4F878) else Color(0xFFE2E8F0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) Color(0xFFE2F8B6) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) LimePrimaryDark else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
            }

            if (isGranted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Granted",
                        tint = StepsGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Allowed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StepsGreen
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0FDF4))
                        .clickable(onClick = onRequestClick)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Allow",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LimePrimaryDark
                    )
                }
            }
        }
    }
}
