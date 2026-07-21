package com.qrforge.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrforge.ui.theme.QRForgeTheme

// ─── Bottom Navigation ────────────────────────────

@Composable
fun QrBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val colors = QRForgeTheme.colors

    data class NavItem(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

    val items = listOf(
        NavItem("home", "Home", Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
        NavItem("templates", "Templates", Icons.Outlined.Widgets, Icons.Filled.Widgets),
        NavItem("history", "History", Icons.Outlined.History, Icons.Filled.History),
        NavItem("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
    )

    Surface(
        color = colors.background2,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.0f else 0.85f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(item.route) }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .scale(scale)
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        tint = if (selected) colors.accent else colors.textTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) colors.accent else colors.textTertiary
                    )
                }
            }
        }
    }
}

// ─── Buttons ──────────────────────────────────────

@Composable
fun QrPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    fullWidth: Boolean = false
) {
    val colors = QRForgeTheme.colors

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accent,
            contentColor = Color(0xFF090909),
            disabledContainerColor = colors.accentMuted,
            disabledContentColor = Color(0xFF090909).copy(alpha = 0.4f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 4.dp
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun QrSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    fullWidth: Boolean = false
) {
    val colors = QRForgeTheme.colors

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.accent
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(colors.accentMuted, colors.accent.copy(alpha = 0.3f)))
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun QrIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = QRForgeTheme.colors.textPrimary
) {
    val colors = QRForgeTheme.colors
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f)
    )

    IconButton(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.background3)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(100)
            pressed = false
        }
    }
}

// ─── Cards ────────────────────────────────────────

@Composable
fun QrCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = QRForgeTheme.colors
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    pressed = true
                    onClick()
                } else Modifier
            ),
        color = colors.background2,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(100)
            pressed = false
        }
    }
}

// ─── Quick Action Item ────────────────────────────

@Composable
fun QrQuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QRForgeTheme.colors
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                pressed = true
                onClick()
            }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.background3),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = colors.accent,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(100)
            pressed = false
        }
    }
}

// ─── Type Chip ────────────────────────────────────

@Composable
fun QrTypeChip(
    icon: ImageVector,
    label: String,
    description: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QRForgeTheme.colors
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.5f)
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                pressed = true
                onClick()
            },
        color = if (selected) colors.accentSurface else colors.background3,
        shape = RoundedCornerShape(16.dp),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, colors.accent)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) colors.accent else colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = colors.textTertiary
                )
            }
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(100)
            pressed = false
        }
    }
}

// ─── Section Header ───────────────────────────────

@Composable
fun QrSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = QRForgeTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.accent,
            letterSpacing = 1.5.sp
        )
        if (action != null && onAction != null) {
            Text(
                text = action,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textTertiary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAction() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// ─── Empty State ──────────────────────────────────

@Composable
fun QrEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = QRForgeTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.accentMuted,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = colors.textTertiary,
            textAlign = TextAlign.Center
        )
        if (action != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            QrPrimaryButton(text = action, onClick = onAction)
        }
    }
}

// ─── Search Bar ───────────────────────────────────

@Composable
fun QrSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    val colors = QRForgeTheme.colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.background3,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(placeholder, color = colors.textTertiary, fontSize = 14.sp)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = colors.accent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Clear",
                        tint = colors.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── History Item ─────────────────────────────────

@Composable
fun QrHistoryItem(
    title: String,
    type: String,
    date: String,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit = {},
    onDuplicate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = QRForgeTheme.colors
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = colors.background2,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accentSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getTypeIcon(type),
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = type,
                        fontSize = 12.sp,
                        color = colors.accent,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = date,
                        fontSize = 12.sp,
                        color = colors.textTertiary
                    )
                }
            }

            IconButton(onClick = onFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) colors.warning else colors.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "More",
                        tint = colors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            showMenu = false
                            onRename()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = {
                            showMenu = false
                            onDuplicate()
                        },
                        leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = colors.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Delete, null, tint = colors.error)
                        }
                    )
                }
            }
        }
    }
}

private fun getTypeIcon(type: String): String = when (type.lowercase()) {
    "url" -> "🔗"
    "text" -> "📝"
    "wifi" -> "📶"
    "email" -> "📧"
    "sms" -> "💬"
    "phone" -> "📞"
    "whatsapp" -> "💚"
    "contact" -> "👤"
    "location" -> "📍"
    "event" -> "📅"
    "play_store" -> "▶️"
    "app_store" -> "🍎"
    "social" -> "🌐"
    else -> "📋"
}

// ─── Category Chip Filter ─────────────────────────

@Composable
fun QrCategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = QRForgeTheme.colors

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .background(if (selected) colors.accent else colors.background3)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color(0xFF090909) else colors.textSecondary
        )
    }
}

// ─── Progress Indicator ───────────────────────────

@Composable
fun QrProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val colors = QRForgeTheme.colors
    val progress by animateFloatAsState(
        targetValue = currentStep.toFloat() / totalSteps,
        animationSpec = tween(400)
    )

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index < currentStep) colors.accent
                        else if (index == currentStep - 1) colors.accent.copy(alpha = progress)
                        else colors.background3
                    )
            )
        }
    }
}

// ─── Template Card ────────────────────────────────

@Composable
fun QrTemplateCard(
    name: String,
    category: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QRForgeTheme.colors

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = colors.background2,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Template preview placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(colors.background3, colors.background2)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("QR", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = category,
                fontSize = 11.sp,
                color = colors.textTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}
