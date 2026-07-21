package com.qrforge.ui.screens.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrforge.domain.model.DotStyle
import com.qrforge.domain.model.EyeShape
import com.qrforge.domain.model.FrameStyle
import com.qrforge.ui.components.*
import com.qrforge.ui.theme.QRForgeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeScreen(
    onQrGenerated: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: CustomizeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = QRForgeTheme.colors
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Color", "Design", "Eyes", "Frame", "Logo")

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.accent, modifier = Modifier.size(32.dp))
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = colors.textPrimary)
            }
            Text("Customize", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Spacer(modifier = Modifier.width(48.dp))
        }

        QrProgressIndicator(currentStep = 3, totalSteps = 4)

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(20.dp)
        ) {
            // Live QR Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(uiState.bgColor.toInt()))
                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.qrBitmap != null) {
                    Image(
                        bitmap = uiState.qrBitmap!!.asImageBitmap(),
                        contentDescription = "QR Code Preview",
                        modifier = Modifier.fillMaxSize(0.7f)
                    )
                } else if (uiState.error != null) {
                    Text(uiState.error!!, color = colors.error, fontSize = 13.sp)
                } else {
                    Text("Generating preview...", color = colors.textTertiary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = colors.accent,
                edgePadding = 0.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(title, fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp, color = if (selectedTab == index) colors.accent else colors.textTertiary)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (selectedTab) {
                0 -> ColorTab(
                    fg = Color(uiState.fgColor.toInt()),
                    bg = Color(uiState.bgColor.toInt()),
                    gradientType = uiState.gradientType,
                    onFgChange = { viewModel.updateFgColor(it.toArgb().toLong() and 0xFFFFFFFFL) },
                    onBgChange = { viewModel.updateBgColor(it.toArgb().toLong() and 0xFFFFFFFFL) },
                    onGradientChange = { viewModel.updateGradientType(it) }
                )
                1 -> DesignTab(uiState.dotStyle) { viewModel.updateDotStyle(it) }
                2 -> EyesTab(uiState.eyeShape) { viewModel.updateEyeShape(it) }
                3 -> FrameTab(uiState.frameStyle) { viewModel.updateFrameStyle(it) }
                4 -> LogoTab(uiState.logoPath) { viewModel.updateLogoPath(it) }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(uiState.error!!, color = colors.error, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            QrPrimaryButton(
                text = if (uiState.isGenerating) "Generating..." else "Generate QR Code",
                onClick = { viewModel.generateAndSave { id -> onQrGenerated(id) } },
                fullWidth = true,
                icon = Icons.Filled.QrCode,
                enabled = !uiState.isGenerating && uiState.qrBitmap != null
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ColorTab(
    fg: Color,
    bg: Color,
    gradientType: String,
    onFgChange: (Color) -> Unit,
    onBgChange: (Color) -> Unit,
    onGradientChange: (String) -> Unit
) {
    val colors = QRForgeTheme.colors
    Column {
        Text("Foreground Color", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        ColorPickerRow(selected = fg, onSelect = onFgChange)
        Spacer(modifier = Modifier.height(20.dp))
        
        Text("Gradient Effect", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("NONE", "LINEAR", "RADIAL").forEach { type ->
                val selected = gradientType == type
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onGradientChange(type) },
                    color = if (selected) colors.accentSurface else colors.background3,
                    shape = RoundedCornerShape(8.dp),
                    border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, colors.accent) else null
                ) {
                    Text(
                        text = when(type) { "NONE" -> "Solid"; "LINEAR" -> "Linear"; "RADIAL" -> "Radial"; else -> type },
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        color = if (selected) colors.accent else colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        
        Text("Background Color", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        ColorPickerRow(selected = bg, onSelect = onBgChange)
    }
}

@Composable
private fun ColorPickerRow(selected: Color, onSelect: (Color) -> Unit) {
    val colorOptions = listOf(
        Color(0xFF090909), Color(0xFF333333), Color(0xFFA3E635),
        Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF3B82F6),
        Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF06B6D4),
        Color(0xFFFFFFFF)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        colorOptions.forEach { color ->
            val isSelected = color == selected
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(color)
                    .border(if (isSelected) 3.dp else 0.dp, if (isSelected) QRForgeTheme.colors.accent else Color.Transparent, CircleShape)
                    .clickable { onSelect(color) }
            )
        }
    }
}

@Composable
private fun DesignTab(dotStyle: DotStyle, onChange: (DotStyle) -> Unit) {
    val colors = QRForgeTheme.colors
    Column {
        Text("Dot Style", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DotStyle.entries.forEach { style ->
                val selected = style == dotStyle
                Surface(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onChange(style) },
                    color = if (selected) colors.accentSurface else colors.background3,
                    shape = RoundedCornerShape(12.dp),
                    border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, colors.accent) else null
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(colors.background2), contentAlignment = Alignment.Center) {
                            Text(when (style) { DotStyle.SQUARE -> "■"; DotStyle.ROUNDED -> "◇"; DotStyle.CIRCULAR -> "●"; DotStyle.DIAMOND -> "◆" }, fontSize = 18.sp, color = colors.accent)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(style.displayName, fontSize = 11.sp, color = if (selected) colors.accent else colors.textTertiary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun EyesTab(eyeShape: EyeShape, onChange: (EyeShape) -> Unit) {
    val colors = QRForgeTheme.colors
    Column {
        Text("Eye Shape", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EyeShape.entries.forEach { shape ->
                val selected = shape == eyeShape
                Surface(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onChange(shape) },
                    color = if (selected) colors.accentSurface else colors.background3,
                    shape = RoundedCornerShape(12.dp),
                    border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, colors.accent) else null
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(colors.background2), contentAlignment = Alignment.Center) {
                            Text(when (shape) { EyeShape.SQUARE -> "⊞"; EyeShape.ROUNDED -> "⊡"; EyeShape.CIRCLE -> "◎"; EyeShape.MODERN -> "◉" }, fontSize = 18.sp, color = colors.accent)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(shape.displayName, fontSize = 11.sp, color = if (selected) colors.accent else colors.textTertiary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun FrameTab(frameStyle: FrameStyle, onChange: (FrameStyle) -> Unit) {
    val colors = QRForgeTheme.colors
    Column {
        Text("Frame Style", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FrameStyle.entries.forEach { style ->
                val selected = style == frameStyle
                Surface(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { onChange(style) },
                    color = if (selected) colors.accentSurface else colors.background3,
                    shape = RoundedCornerShape(10.dp),
                    border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, colors.accent) else null
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(6.dp)).background(colors.background2), contentAlignment = Alignment.Center) {
                            Text(when (style) {
                                FrameStyle.NONE -> "☐"
                                FrameStyle.THIN -> "▭"
                                FrameStyle.THICK -> "▢"
                                FrameStyle.ROUNDED -> "⏠"
                                FrameStyle.BADGE -> "📛"
                                FrameStyle.STICKER -> "🏷"
                            }, fontSize = 16.sp, color = colors.accent)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(style.displayName.split(" ")[0], fontSize = 10.sp, color = if (selected) colors.accent else colors.textTertiary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoTab(logoPath: String?, onChange: (String?) -> Unit) {
    val colors = QRForgeTheme.colors
    val logos = listOf(
        Pair(null, "None"),
        Pair("LINK", "Link"),
        Pair("WIFI", "WiFi"),
        Pair("INSTAGRAM", "Insta"),
        Pair("WHATSAPP", "WApp"),
        Pair("YOUTUBE", "YouTube")
    )
    Column {
        Text("Center Logo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            logos.forEach { (path, label) ->
                val selected = logoPath == path
                Surface(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { onChange(path) },
                    color = if (selected) colors.accentSurface else colors.background3,
                    shape = RoundedCornerShape(10.dp),
                    border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, colors.accent) else null
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(6.dp)).background(colors.background2), contentAlignment = Alignment.Center) {
                            Text(when (path) {
                                null -> "Ø"
                                "LINK" -> "🔗"
                                "WIFI" -> "📶"
                                "INSTAGRAM" -> "📸"
                                "WHATSAPP" -> "💬"
                                "YOUTUBE" -> "▶"
                                else -> "★"
                            }, fontSize = 16.sp, color = colors.accent)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(label, fontSize = 9.sp, color = if (selected) colors.accent else colors.textTertiary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
