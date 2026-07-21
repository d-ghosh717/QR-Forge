package com.qrforge.ui.screens.detail

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrforge.ui.components.*
import com.qrforge.ui.theme.QRForgeTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrDetailScreen(
    historyId: Long,
    onBack: () -> Unit,
    viewModel: QrDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = QRForgeTheme.colors
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRenameDialog by remember { mutableStateOf(false) }

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

    if (uiState.error != null) {
        QrEmptyState(icon = Icons.Outlined.ErrorOutline, title = "Error", subtitle = uiState.error!!, modifier = Modifier.fillMaxSize())
        return
    }

    // Delete confirmation
    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirm() },
            title = { Text("Delete QR Code") },
            text = { Text("Are you sure you want to delete \"${uiState.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete()
                    onBack()
                }) { Text("Delete", color = colors.error) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirm() }) { Text("Cancel") }
            }
        )
    }

    // Rename dialog
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(uiState.title) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rename(newName)
                    showRenameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = colors.textPrimary)
                }
                Text("QR Details", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Row {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(if (uiState.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            "Favorite", tint = if (uiState.isFavorite) colors.warning else colors.textTertiary)
                    }
                    IconButton(onClick = { viewModel.showDeleteConfirm() }) {
                        Icon(Icons.Outlined.Delete, "Delete", tint = colors.error, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // QR Image
            Box(
                modifier = Modifier.fillMaxWidth(0.7f).aspectRatio(1f).clip(RoundedCornerShape(24.dp)).background(Color.White)
                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.qrBitmap != null) {
                    Image(uiState.qrBitmap!!.asImageBitmap(), "QR Code", modifier = Modifier.fillMaxSize(0.7f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info
            Surface(modifier = Modifier.fillMaxWidth(), color = colors.background2, shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow("Title", uiState.title)
                    Divider(color = colors.surfaceBorder, modifier = Modifier.padding(vertical = 10.dp))
                    InfoRow("Type", uiState.type.replaceFirstChar { it.uppercase() })
                    Divider(color = colors.surfaceBorder, modifier = Modifier.padding(vertical = 10.dp))
                    InfoRow("Content", uiState.content.take(80) + if (uiState.content.length > 80) "..." else "")
                    Divider(color = colors.surfaceBorder, modifier = Modifier.padding(vertical = 10.dp))
                    InfoRow("Created", SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date(uiState.createdAt)))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QrSecondaryButton("Share", onClick = { viewModel.sharePng() }, modifier = Modifier.weight(1f), icon = Icons.Outlined.Share)
                QrPrimaryButton("Download", onClick = { viewModel.downloadPng() }, modifier = Modifier.weight(1f), icon = Icons.Outlined.Download, enabled = !uiState.isExporting)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionChip("Rename", Icons.Outlined.DriveFileRenameOutline) { showRenameDialog = true }
                ActionChip("Delete", Icons.Outlined.Delete) { viewModel.showDeleteConfirm() }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = QRForgeTheme.colors
    Column {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.accent, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
    }
}

@Composable
private fun RowScope.ActionChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val colors = QRForgeTheme.colors
    Surface(
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).clickable { onClick() },
        color = colors.background3, shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = colors.accent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
        }
    }
}
