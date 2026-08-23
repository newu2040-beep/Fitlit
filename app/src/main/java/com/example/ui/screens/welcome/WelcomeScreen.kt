package com.example.ui.screens.welcome

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.LiquidGlassButton
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.TextOnLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onLoginClick: (() -> Unit)? = null
) {
    // Staggered intro animation state for smooth, gentle entrance
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Slow, graceful fade and slide transitions
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1100, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )
    val logoOffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 40f,
        animationSpec = tween(durationMillis = 1100, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "logoOffsetY"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "textAlpha"
    )
    val textOffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 30f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "textOffsetY"
    )

    val ctaAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 700, easing = FastOutSlowInEasing),
        label = "ctaAlpha"
    )
    val ctaOffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 25f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 700, easing = FastOutSlowInEasing),
        label = "ctaOffsetY"
    )

    // Gentle, slow ambient floating and pulsing glow (3500ms cycle)
    val infiniteTransition = rememberInfiniteTransition(label = "ambientGlow")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Decorative background liquid glass image
        Image(
            painter = painterResource(id = R.drawable.welcome_hero_glass),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.45f
        )

        // Subtle gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x60FFFFFF),
                        0.5f to Color(0x90FFFFFF),
                        1.0f to Color(0xF5FFFFFF)
                    )
                )
        )

        // Main Content centered and aligned
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Center Branding with slow smooth animation & gentle floating
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .alpha(logoAlpha)
                    .offset { IntOffset(0, logoOffsetY.roundToInt()) }
            ) {
                // Floating container with ambient aura
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.offset { IntOffset(0, floatOffset.roundToInt()) }
                ) {
                    // Soft glowing aura behind the icon
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(glowScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        LimePrimary.copy(alpha = glowAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Liquid glass Fitlit Icon
                    Image(
                        painter = painterResource(id = R.drawable.app_icon_foreground_1787458633138),
                        contentDescription = "Fitlit Logo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(32.dp))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .alpha(textAlpha)
                        .offset { IntOffset(0, textOffsetY.roundToInt()) }
                ) {
                    Text(
                        text = "Fitlit",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        color = TextPrimary
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "AI-powered fitness.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Personalized for",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextSecondary
                            )
                            Text(
                                text = "you.",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LimePrimaryDark
                            )
                        }
                    }
                }
            }

            // Bottom CTA Button (Smooth delayed slide up, login row removed)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(ctaAlpha)
                    .offset { IntOffset(0, ctaOffsetY.roundToInt()) }
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassButton(
                    onClick = onGetStartedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("welcome_get_started_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Get Started",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnLime
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = TextOnLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
