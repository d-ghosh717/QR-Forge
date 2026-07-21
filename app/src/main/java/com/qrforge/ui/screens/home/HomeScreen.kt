package com.qrforge.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrforge.domain.model.QrType
import com.qrforge.ui.components.*
import com.qrforge.ui.theme.QRForgeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEnterData: (String) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = QRForgeTheme.colors

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.qrforge.R.drawable.ic_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("QR Forge", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                        color = colors.textPrimary, letterSpacing = (-0.5).sp)
                    Text("Create beautiful QR codes", fontSize = 14.sp, color = colors.textTertiary)
                }
            }
        }

        // Hero Card
        item {
            Box(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge)) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(Brush.linearGradient(listOf(colors.accentSurface, colors.background2, colors.background3)))
                        .clickable { onNavigateToCreate() }
                        .padding(24.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Create New QR", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Generate a custom QR code\nwith your own style", fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Get started", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.accent)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.ArrowForward, null, tint = colors.accent, modifier = Modifier.size(16.dp))
                            }
                        }
                        Box(modifier = Modifier.size(72.dp).clip(MaterialTheme.shapes.medium).background(colors.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.QrCode, null, tint = colors.accent, modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
        }

        // Quick Actions
        item {
            QrSectionHeader("QUICK CREATE")
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickAction("URL", Icons.Filled.Link) { onNavigateToEnterData("url") }
                QuickAction("Text", Icons.Filled.TextFields) { onNavigateToEnterData("text") }
                QuickAction("WiFi", Icons.Filled.Wifi) { onNavigateToEnterData("wifi") }
                QuickAction("Contact", Icons.Filled.ContactPage) { onNavigateToEnterData("contact") }
                QuickAction("Email", Icons.Filled.Email) { onNavigateToEnterData("email") }
                QuickAction("SMS", Icons.Filled.Sms) { onNavigateToEnterData("sms") }
            }
        }

        // Recent History
        if (uiState.recentHistory.isNotEmpty()) {
            item {
                QrSectionHeader("RECENT", action = "See All", onAction = onNavigateToHistory)
                Spacer(modifier = Modifier.height(12.dp))

                uiState.recentHistory.forEach { item ->
                    QrHistoryItem(
                        title = item.title,
                        type = item.type,
                        date = formatRelativeTime(item.createdAt),
                        isFavorite = item.isFavorite,
                        onClick = { onNavigateToDetail(item.id) },
                        onFavorite = { viewModel.toggleFavorite(item.id, item.isFavorite) },
                        onDelete = { viewModel.deleteItem(item.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Templates Preview
        if (uiState.templates.isNotEmpty()) {
            item {
                QrSectionHeader("TEMPLATES", action = "See All", onAction = onNavigateToTemplates)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.templates.take(5)) { template ->
                        QrTemplateCard(
                            name = template.name,
                            category = template.category,
                            onClick = { onNavigateToTemplates() },
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val colors = QRForgeTheme.colors
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f))

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale).clip(RoundedCornerShape(16.dp)).clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null
        ) { pressed = true; onClick() }.padding(8.dp)
    ) {
        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(colors.background3),
            contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = colors.accent, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
    }
    LaunchedEffect(pressed) { if (pressed) { kotlinx.coroutines.delay(100); pressed = false } }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
    }
}
