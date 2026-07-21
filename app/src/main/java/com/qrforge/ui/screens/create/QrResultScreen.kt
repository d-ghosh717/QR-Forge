package com.qrforge.ui.screens.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrforge.ui.components.*
import com.qrforge.ui.theme.QRForgeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrResultScreen(
    onBack: () -> Unit,
    onBackToHome: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = QRForgeTheme.colors
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearExportMessage()
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.accent, modifier = Modifier.size(32.dp))
        }
        return
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Success icon
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                    .background(colors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, null, tint = colors.accent, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("QR Code Generated!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text(uiState.title, fontSize = 14.sp, color = colors.textTertiary)

            Spacer(modifier = Modifier.height(20.dp))

            // QR Image
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.qrBitmap != null) {
                    Image(
                        bitmap = uiState.qrBitmap!!.asImageBitmap(),
                        contentDescription = "Generated QR Code",
                        modifier = Modifier.fillMaxSize(0.7f)
                    )
                } else {
                    Text("Generating...", color = colors.textTertiary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Share + Download row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QrSecondaryButton(
                    text = "Share",
                    onClick = { viewModel.sharePng() },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Share,
                    enabled = !uiState.isExporting && uiState.qrBitmap != null
                )
                QrPrimaryButton(
                    text = "Download",
                    onClick = { viewModel.exportPng() },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Download,
                    enabled = !uiState.isExporting && uiState.qrBitmap != null
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Format-specific export
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormatChip("PNG", Icons.Outlined.Image, enabled = !uiState.isExporting && uiState.qrBitmap != null) { viewModel.exportPng() }
                FormatChip("SVG", Icons.Outlined.Code, enabled = !uiState.isExporting && uiState.qrBitmap != null) { viewModel.exportPng() }
                FormatChip("PDF", Icons.Outlined.PictureAsPdf, enabled = !uiState.isExporting && uiState.qrBitmap != null) { viewModel.exportPng() }
            }

            if (uiState.isExporting) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(color = colors.accent, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            QrSecondaryButton(
                text = "Create Another QR",
                onClick = onBack,
                fullWidth = true,
                icon = Icons.Outlined.Add
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = onBackToHome) {
                Text("Back to Home", color = colors.textTertiary, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RowScope.FormatChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val colors = QRForgeTheme.colors
    val alpha = if (enabled) 1.0f else 0.5f
    Surface(
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).clickable(enabled = enabled) { onClick() },
        color = colors.background3.copy(alpha = alpha),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp).alpha(alpha), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = colors.accent, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
        }
    }
}
