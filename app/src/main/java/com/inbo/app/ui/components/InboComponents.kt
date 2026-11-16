package com.inbo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inbo.app.ui.theme.*

/**
 * Standard Inbo Top Bar with logo, tagline and user avatar.
 * Dynamically responds to InboTheme (light or dark mode).
 * Tagline: "LESS NOISE. MORE LIFE."
 */
@Composable
fun InboTopBar(
    modifier: Modifier = Modifier,
    userInitial: String = "G",
    onAvatarClick: () -> Unit = {}
) {
    val theme = InboTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "inbo",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    letterSpacing = (-0.5).sp
                )
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp, start = 2.dp)
                        .size(5.dp)
                        .background(theme.accentPurple, CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "LESS NOISE. MORE LIFE.",
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
                color = theme.textMuted
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(theme.softPurple)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userInitial,
                color = theme.accentPurple,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Standard uppercase eyebrow section header (e.g. "YOUR INBOX", "YOUR TASKS").
 */
@Composable
fun InboSectionEyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = InboTheme.colors.accentPurple
) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

/**
 * Standard screen hero header with eyebrow, title, and descriptive subtitle.
 */
@Composable
fun InboScreenHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val theme = InboTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        InboSectionEyebrow(text = eyebrow)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 30.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = theme.textMuted
        )
    }
}

/**
 * Consistent Inbo Card with subtle border and rounded corners.
 * Automatically adopts the active theme's card surface and border colors.
 */
@Composable
fun InboCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    containerColor: Color = InboTheme.colors.cardSurface,
    borderColor: Color = InboTheme.colors.cardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Card(
        modifier = cardModifier.border(1.dp, borderColor, RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            content = content
        )
    }
}

/**
 * Summary stat card used for quick metrics (e.g. Inbox summary, task stats).
 */
@Composable
fun InboStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accentColor: Color = InboTheme.colors.accentPurple,
    onClick: (() -> Unit)? = null
) {
    val theme = InboTheme.colors

    InboCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 10.sp,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(accentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = theme.textMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Pill badge for tags, statuses, categories.
 */
@Composable
fun InboBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = InboTheme.colors.softPurple,
    contentColor: Color = InboTheme.colors.accentPurple
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

/**
 * Interactive filter chip for list views.
 */
@Composable
fun InboFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = InboTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) theme.accentPurple else theme.cardSurface)
            .border(
                1.dp,
                if (selected) theme.accentPurple else theme.cardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else theme.textMuted
        )
    }
}

/**
 * Standard Inbo Primary Button.
 */
@Composable
fun InboButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val theme = InboTheme.colors

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = theme.accentPurple,
            contentColor = Color.White,
            disabledContainerColor = theme.softPurple,
            disabledContentColor = theme.textMuted
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
