package com.qrforge.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrforge.ui.components.*
import com.qrforge.ui.theme.QRForgeTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateToDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = QRForgeTheme.colors
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    var showRenameDialog by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("History", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)

            var showSortMenu by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { showSortMenu = true }) {
                    Text(uiState.sortOrder.displayName, fontSize = 13.sp, color = colors.accent)
                    Icon(Icons.Filled.ArrowDropDown, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = { Text(order.displayName) },
                            onClick = { viewModel.onSortChange(order); showSortMenu = false },
                            leadingIcon = {
                                if (order == uiState.sortOrder) Icon(Icons.Filled.Check, null, tint = colors.accent)
                            }
                        )
                    }
                }
            }
        }

        QrSearchBar(query = uiState.searchQuery, onQueryChange = { viewModel.onSearch(it) },
            modifier = Modifier.padding(horizontal = 20.dp))

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accent, modifier = Modifier.size(32.dp))
            }
        } else if (uiState.items.isEmpty()) {
            QrEmptyState(
                icon = Icons.Outlined.History,
                title = if (uiState.searchQuery.isBlank()) "No QR codes yet" else "No results found",
                subtitle = if (uiState.searchQuery.isBlank()) "Generated QR codes will appear here" else "Try a different search term"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    QrHistoryItem(
                        title = item.title,
                        type = item.type.replaceFirstChar { it.uppercase() },
                        date = formatDate(item.createdAt),
                        isFavorite = item.isFavorite,
                        onClick = { onNavigateToDetail(item.id) },
                        onFavorite = { viewModel.toggleFavorite(item.id, item.isFavorite) },
                        onDelete = { showDeleteDialog = item.id },
                        onRename = { showRenameDialog = item.id },
                        onDuplicate = { viewModel.duplicateItem(item.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // Delete confirmation
    showDeleteDialog?.let { id ->
        val item = uiState.items.find { it.id == id }
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete QR Code") },
            text = { Text("Delete \"${item?.title ?: "this QR code"}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteItem(id); showDeleteDialog = null }) {
                    Text("Delete", color = colors.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }

    // Rename dialog
    showRenameDialog?.let { id ->
        val item = uiState.items.find { it.id == id }
        var newName by remember { mutableStateOf(item?.title ?: "") }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename QR Code") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        cursorColor = colors.accent
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        viewModel.rename(id, newName.trim())
                    }
                    showRenameDialog = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") } }
        )
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
}
