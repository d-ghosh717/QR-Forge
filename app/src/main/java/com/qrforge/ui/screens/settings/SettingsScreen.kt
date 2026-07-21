package com.qrforge.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrforge.ui.components.QrSectionHeader
import com.qrforge.ui.theme.QRForgeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = QRForgeTheme.colors

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        // Export Quality
        item {
            QrSectionHeader(title = "EXPORT")
            Spacer(modifier = Modifier.height(8.dp))
            QrSettingsCard {
                QrDropdownSetting(
                    icon = Icons.Outlined.HighQuality,
                    title = "Export Quality",
                    subtitle = "Default PNG resolution",
                    value = uiState.exportQuality,
                    options = listOf("Low", "Medium", "High", "Ultra"),
                    onSelect = { viewModel.setExportQuality(it) }
                )
            }
        }

        // Default Style
        item {
            QrSectionHeader(title = "DEFAULT STYLE")
            Spacer(modifier = Modifier.height(8.dp))
            QrSettingsCard {
                QrDropdownSetting(
                    icon = Icons.Outlined.Palette,
                    title = "Dot Style",
                    subtitle = "Default dot pattern",
                    value = uiState.defaultDotStyle,
                    options = listOf("Square", "Rounded", "Circular", "Diamond"),
                    onSelect = { viewModel.setDefaultDotStyle(it) }
                )
                Divider(color = colors.surfaceBorder, modifier = Modifier.padding(vertical = 12.dp))
                QrDropdownSetting(
                    icon = Icons.Outlined.Visibility,
                    title = "Eye Shape",
                    subtitle = "Default finder pattern style",
                    value = uiState.defaultEyeShape,
                    options = listOf("Square", "Rounded", "Circle", "Modern"),
                    onSelect = { viewModel.setDefaultEyeShape(it) }
                )
            }
        }

        // Default Colors
        item {
            QrSectionHeader(title = "DEFAULT COLORS")
            Spacer(modifier = Modifier.height(8.dp))
            QrSettingsCard {
                QrColorPreviewSetting(
                    title = "Foreground Color",
                    color = uiState.defaultFgColor
                )
                Divider(color = colors.surfaceBorder, modifier = Modifier.padding(vertical = 12.dp))
                QrColorPreviewSetting(
                    title = "Background Color",
                    color = uiState.defaultBgColor
                )
            }
        }

        // About
        item {
            QrSectionHeader(title = "ABOUT")
            Spacer(modifier = Modifier.height(8.dp))
            QrSettingsCard {
                QrInfoRow(
                    icon = Icons.Outlined.Info,
                    title = "Version",
                    subtitle = uiState.appVersion
                )
                Divider(color = colors.surfaceBorder, modifier = Modifier.padding(vertical = 12.dp))
                QrInfoRow(
                    icon = Icons.Outlined.Shield,
                    title = "Privacy Policy",
                    subtitle = "View",
                    onClick = { /* Open privacy policy */ }
                )
                Divider(color = colors.surfaceBorder, modifier = Modifier.padding(vertical = 12.dp))
                QrInfoRow(
                    icon = Icons.Outlined.Description,
                    title = "Terms of Service",
                    subtitle = "View",
                    onClick = { /* Open terms */ }
                )
            }
        }

        // Footer
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.qrforge.R.drawable.ic_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "QR Forge v${uiState.appVersion}",
                fontSize = 12.sp,
                color = colors.textTertiary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "Made with ❤️",
                fontSize = 11.sp,
                color = colors.accentMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ─── Settings Components ──────────────────────────

@Composable
fun QrSettingsCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = QRForgeTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.background2,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun QrDropdownSetting(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    val colors = QRForgeTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = colors.accent, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = colors.textPrimary)
                Text(subtitle, fontSize = 12.sp, color = colors.textTertiary)
            }
        }

        Box {
            TextButton(onClick = { expanded = true }) {
                Text(value, fontSize = 14.sp, color = colors.accent, fontWeight = FontWeight.SemiBold)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                        leadingIcon = {
                            if (option == value) {
                                Icon(Icons.Filled.Check, null, tint = colors.accent)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QrColorPreviewSetting(
    title: String,
    color: String
) {
    val colors = QRForgeTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Circle, null, tint = colors.accent, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = colors.textPrimary)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(color, fontSize = 13.sp, color = colors.textTertiary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(color)))
                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(6.dp))
            )
        }
    }
}

@Composable
fun QrInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    val colors = QRForgeTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = colors.accent, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = colors.textPrimary)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(subtitle, fontSize = 13.sp, color = colors.textTertiary)
            if (onClick != null) {
                Icon(
                    Icons.Filled.ChevronRight, null,
                    tint = colors.textTertiary, modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
