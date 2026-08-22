package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.LimeGradientEnd
import com.example.ui.theme.LimeGradientStart
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.TextOnLime
import com.example.ui.theme.TextPrimary

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    backgroundColor: Color = GlassSurfaceLight,
    borderColor: Color = GlassBorderLight,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0x0A000000),
                spotColor = Color(0x14A6E324)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                border = BorderStroke(borderWidth, borderColor),
                shape = shape
            )
            .then(clickableModifier)
    ) {
        content()
    }
}

@Composable
fun LimeGradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFF6FDE8),
            Color(0xFFEBFCD2),
            Color(0xFFFAFEF2)
        )
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = Color(0x0C000000),
                spotColor = Color(0x28A6E324)
            )
            .clip(shape)
            .background(gradient)
            .border(
                border = BorderStroke(1.dp, Color(0x66D4F878)),
                shape = shape
            )
            .then(clickableModifier)
    ) {
        content()
    }
}

@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(32.dp),
    content: @Composable () -> Unit
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            LimeGradientStart,
            LimeGradientEnd
        )
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color(0x14000000),
                spotColor = Color(0x40A6E324)
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = TextOnLime
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .border(1.dp, Color(0x66FFFFFF), shape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun GlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val background = if (selected) {
        Brush.horizontalGradient(listOf(LimeGradientStart, LimeGradientEnd))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF9FAFB)))
    }
    val textColor = if (selected) TextOnLime else TextPrimary
    val borderColor = if (selected) Color(0x4086C210) else GlassBorderLight

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = modifier
            .shadow(if (selected) 4.dp else 1.dp, RoundedCornerShape(20.dp), spotColor = Color(0x20A6E324))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .background(background)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}
