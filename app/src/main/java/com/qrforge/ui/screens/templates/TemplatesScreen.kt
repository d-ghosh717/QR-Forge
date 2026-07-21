package com.qrforge.ui.screens.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrforge.ui.components.*
import com.qrforge.ui.theme.QRForgeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: TemplatesViewModel,
    onNavigateToPreview: (Long) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = QRForgeTheme.colors

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Templates", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.categories) { category ->
                QrCategoryChip(
                    label = category,
                    selected = category == uiState.selectedCategory,
                    onClick = { viewModel.selectCategory(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accent, modifier = Modifier.size(32.dp))
            }
        } else if (uiState.templates.isEmpty()) {
            QrEmptyState(
                icon = Icons.Outlined.Widgets,
                title = "No templates yet",
                subtitle = "Create your first QR code to get started",
                action = "Create QR",
                onAction = onNavigateToCreate
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.templates, key = { it.id }) { template ->
                    QrTemplateCard(
                        name = template.name,
                        category = template.category,
                        onClick = { onNavigateToPreview(template.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplatePreviewScreen(
    templateId: Long,
    onApply: (String, Long) -> Unit,
    onBack: () -> Unit,
    templatesViewModel: TemplatesViewModel = hiltViewModel()
) {
    val colors = QRForgeTheme.colors
    var template by remember { mutableStateOf<com.qrforge.domain.model.Template?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(templateId) {
        template = templatesViewModel.getTemplateById(templateId)
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = colors.textPrimary)
            }
            Text("Template Preview", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accent, modifier = Modifier.size(32.dp))
            }
        } else if (template != null) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(MaterialTheme.shapes.extraLarge)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material.icons.Icons.Filled.QrCode.let {
                        Icon(it, null, tint = Color(0xFF090909), modifier = Modifier.size(80.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(template!!.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF090909))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(template!!.category, fontSize = 13.sp, color = Color(0xFF666666))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Type: ${template!!.qrType}", fontSize = 12.sp, color = Color(0xFF999999))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Style Details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(color = colors.background2, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StyleRow("Dot Style", template!!.dotStyle.replaceFirstChar { it.uppercase() })
                    StyleRow("Eye Shape", template!!.eyeShape.replaceFirstChar { it.uppercase() })
                    StyleRow("Frame", template!!.frameStyle.replaceFirstChar { it.uppercase() })
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            QrPrimaryButton(
                text = "Apply This Template",
                onClick = { onApply(template!!.qrType.lowercase(), template!!.id) },
                fullWidth = true,
                icon = Icons.Filled.Check
            )
        } else {
            Text("Template not found", color = colors.textTertiary)
        }
    }
}

@Composable
private fun StyleRow(label: String, value: String) {
    val colors = QRForgeTheme.colors
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = colors.textSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.accent)
    }
}
