package com.qrforge.ui.screens.create

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrforge.domain.model.QrType
import com.qrforge.ui.components.*
import com.qrforge.ui.theme.QRForgeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseTypeScreen(
    onTypeSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val colors = QRForgeTheme.colors

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.Close, "Close", tint = colors.textPrimary)
            }
            Text("Create QR", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Spacer(modifier = Modifier.width(48.dp))
        }

        QrProgressIndicator(currentStep = 1, totalSteps = 4)
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choose QR Type",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Text(
            text = "Select what kind of QR code you want to create",
            fontSize = 13.sp,
            color = colors.textTertiary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val types = listOf(
                QrType.URL to Icons.Filled.Link,
                QrType.TEXT to Icons.Filled.TextFields,
                QrType.WIFI to Icons.Filled.Wifi,
                QrType.EMAIL to Icons.Filled.Email,
                QrType.SMS to Icons.Filled.Sms,
                QrType.PHONE to Icons.Filled.Phone,
                QrType.WHATSAPP to Icons.Outlined.Chat,
                QrType.CONTACT to Icons.Filled.ContactPage,
                QrType.LOCATION to Icons.Filled.LocationOn,
                QrType.EVENT to Icons.Filled.Event,
                QrType.PLAY_STORE to Icons.Outlined.PlayArrow,
                QrType.APP_STORE to Icons.Outlined.PhoneIphone,
                QrType.SOCIAL to Icons.Filled.Share,
                QrType.CUSTOM to Icons.Filled.Build
            )

            items(types) { (type, icon) ->
                QrTypeChip(
                    icon = icon,
                    label = type.displayName,
                    description = type.description,
                    onClick = { onTypeSelected(type.name.lowercase()) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
