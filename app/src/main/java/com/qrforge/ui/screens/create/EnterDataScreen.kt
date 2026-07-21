package com.qrforge.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrforge.domain.model.QrType
import com.qrforge.ui.components.*
import com.qrforge.ui.theme.QRForgeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterDataScreen(
    onDataEntered: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: EnterDataViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = QRForgeTheme.colors
    val scrollState = rememberScrollState()
    val type = uiState.qrType
    var formData by rememberSaveable { mutableStateOf(mutableMapOf<String, String>()) }

    // Sync form data to ViewModel whenever it changes
    LaunchedEffect(formData) {
        viewModel.updateFormData(formData)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = colors.textPrimary)
            }
            Text(type.displayName, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Spacer(modifier = Modifier.width(48.dp))
        }

        QrProgressIndicator(currentStep = 2, totalSteps = 4)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text("Enter Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text(
                "Fill in the information for your ${type.displayName} QR code",
                fontSize = 13.sp, color = colors.textTertiary
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (type) {
                QrType.URL -> FormField("URL", "https://example.com", KeyboardType.Uri) {
                    formData = formData.toMutableMap().apply { put("url", it) }
                }
                QrType.TEXT -> FormField("Text", "Enter your text", KeyboardType.Text, multiLine = true) {
                    formData = formData.toMutableMap().apply { put("text", it) }
                }
                QrType.WIFI -> {
                    FormField("Network Name (SSID)", "MyWiFi", KeyboardType.Text) {
                        formData = formData.toMutableMap().apply { put("ssid", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Password", "Enter password", KeyboardType.Password) {
                        formData = formData.toMutableMap().apply { put("password", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SecuritySelector { security ->
                        formData = formData.toMutableMap().apply { put("security", security) }
                    }
                }
                QrType.EMAIL -> {
                    FormField("Email Address", "hello@example.com", KeyboardType.Email) {
                        formData = formData.toMutableMap().apply { put("address", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Subject", "Hello", KeyboardType.Text) {
                        formData = formData.toMutableMap().apply { put("subject", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Body", "Write your message...", KeyboardType.Text, multiLine = true) {
                        formData = formData.toMutableMap().apply { put("body", it) }
                    }
                }
                QrType.SMS -> {
                    FormField("Phone Number", "+1234567890", KeyboardType.Phone) {
                        formData = formData.toMutableMap().apply { put("phone", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Message", "Your message...", KeyboardType.Text, multiLine = true) {
                        formData = formData.toMutableMap().apply { put("message", it) }
                    }
                }
                QrType.PHONE -> FormField("Phone Number", "+1234567890", KeyboardType.Phone) {
                    formData = formData.toMutableMap().apply { put("phone", it) }
                }
                QrType.WHATSAPP -> {
                    FormField("Phone Number (with country code)", "+1234567890", KeyboardType.Phone) {
                        formData = formData.toMutableMap().apply { put("phone", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Message (optional)", "Hi!", KeyboardType.Text) {
                        formData = formData.toMutableMap().apply { put("message", it) }
                    }
                }
                QrType.CONTACT -> {
                    FormField("Full Name", "John Doe", KeyboardType.Text) {
                        formData = formData.toMutableMap().apply { put("name", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Organization", "Company Inc.", KeyboardType.Text) {
                        formData = formData.toMutableMap().apply { put("organization", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Phone", "+1234567890", KeyboardType.Phone) {
                        formData = formData.toMutableMap().apply { put("phone", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Email", "john@example.com", KeyboardType.Email) {
                        formData = formData.toMutableMap().apply { put("email", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Website", "https://example.com", KeyboardType.Uri) {
                        formData = formData.toMutableMap().apply { put("website", it) }
                    }
                }
                QrType.LOCATION -> {
                    FormField("Latitude", "37.7749", KeyboardType.Decimal) {
                        formData = formData.toMutableMap().apply { put("latitude", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Longitude", "-122.4194", KeyboardType.Decimal) {
                        formData = formData.toMutableMap().apply { put("longitude", it) }
                    }
                }
                QrType.EVENT -> {
                    FormField("Event Title", "Birthday Party", KeyboardType.Text) {
                        formData = formData.toMutableMap().apply { put("title", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Description", "Come celebrate!", KeyboardType.Text, multiLine = true) {
                        formData = formData.toMutableMap().apply { put("description", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Location", "123 Main St", KeyboardType.Text) {
                        formData = formData.toMutableMap().apply { put("location", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("Start", "20250101T120000", KeyboardType.Text) {
                        formData = formData.toMutableMap().apply { put("start", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormField("End", "20250101T140000", KeyboardType.Text) {
                        formData = formData.toMutableMap().apply { put("end", it) }
                    }
                }
                QrType.PLAY_STORE -> FormField("Package Name", "com.example.app", KeyboardType.Text) {
                    formData = formData.toMutableMap().apply { put("package", it) }
                }
                QrType.APP_STORE -> FormField("App Store URL", "https://apps.apple.com/app/id123", KeyboardType.Uri) {
                    formData = formData.toMutableMap().apply { put("url", it) }
                }
                QrType.SOCIAL -> FormField("Profile URL", "https://", KeyboardType.Uri) {
                    formData = formData.toMutableMap().apply { put("url", it) }
                }
                QrType.CUSTOM -> FormField("Custom Content", "Enter your data", KeyboardType.Text, multiLine = true) {
                    formData = formData.toMutableMap().apply { put("content", it) }
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(uiState.error!!, color = colors.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            QrPrimaryButton(
                text = if (uiState.isSaving) "Saving..." else "Continue",
                onClick = { viewModel.submitAndSave { id -> onDataEntered(id) } },
                fullWidth = true,
                icon = Icons.Filled.ArrowForward,
                enabled = viewModel.isFormValid() && !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FormField(label: String, placeholder: String, keyboardType: KeyboardType, multiLine: Boolean = false, onValueChange: (String) -> Unit) {
    val colors = QRForgeTheme.colors
    var value by remember { mutableStateOf("") }

    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(color = colors.background3, shape = RoundedCornerShape(14.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it; onValueChange(it) },
                placeholder = { Text(placeholder, color = colors.textTertiary, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.accent
                ),
                singleLine = !multiLine,
                maxLines = if (multiLine) 4 else 1,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecuritySelector(onSelect: (String) -> Unit) {
    val colors = QRForgeTheme.colors
    var selected by remember { mutableStateOf("WPA") }
    val options = listOf("WPA", "WPA2", "WEP", "None")

    Column {
        Text("Security", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { selected = option; onSelect(option) },
                    label = { Text(option, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.accentSurface,
                        selectedLabelColor = colors.accent
                    )
                )
            }
        }
    }
}
