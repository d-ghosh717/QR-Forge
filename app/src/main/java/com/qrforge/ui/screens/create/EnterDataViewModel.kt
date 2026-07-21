package com.qrforge.ui.screens.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrforge.domain.model.QrType
import com.qrforge.repository.QrHistoryRepository
import com.qrforge.repository.QrTemplateRepository
import com.qrforge.domain.model.QrHistoryItem
import com.qrforge.util.QrContentEncoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnterDataUiState(
    val qrType: QrType = QrType.URL,
    val formData: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EnterDataViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val historyRepository: QrHistoryRepository,
    private val templateRepository: QrTemplateRepository
) : ViewModel() {

    private val qrTypeArg: String = savedStateHandle.get<String>("qrType") ?: "url"
    private val templateId: Long = savedStateHandle.get<Long>("templateId") ?: -1L

    private val _uiState = MutableStateFlow(
        EnterDataUiState(qrType = QrType.fromName(qrTypeArg))
    )
    val uiState: StateFlow<EnterDataUiState> = _uiState.asStateFlow()

    fun updateFormData(data: Map<String, String>) {
        _uiState.value = _uiState.value.copy(formData = data)
    }

    fun isFormValid(): Boolean {
        return isFormValidInternal(_uiState.value.qrType, _uiState.value.formData)
    }

    fun submitAndSave(onSaved: (Long) -> Unit) {
        val state = _uiState.value
        if (state.isSaving) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)

            try {
                val encoded = QrContentEncoder.encode(state.qrType, state.formData)
                if (encoded.isBlank()) {
                    _uiState.value = state.copy(isSaving = false, error = "Content cannot be empty")
                    return@launch
                }

                val title = generateTitle(state.qrType, state.formData)
                val rawDataJson = state.formData.entries.joinToString(",") { "${it.key}=${it.value}" }

                val template = if (templateId != -1L) {
                    templateRepository.getById(templateId)
                } else null

                val item = QrHistoryItem(
                    type = state.qrType.name,
                    title = title,
                    content = encoded,
                    rawData = rawDataJson,
                    foregroundColor = template?.foregroundColor ?: 0xFF000000,
                    backgroundColor = template?.backgroundColor ?: 0xFFFFFFFF,
                    dotStyle = template?.dotStyle ?: "ROUNDED",
                    eyeShape = template?.eyeShape ?: "MODERN",
                    frameStyle = template?.frameStyle ?: "NONE",
                    logoPath = template?.logoPath
                )

                val id = historyRepository.insert(item)
                onSaved(id)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to create QR code"
                )
            }
        }
    }

    private fun generateTitle(type: QrType, data: Map<String, String>): String {
        return when (type) {
            QrType.URL -> data["url"]?.let { url ->
                try { java.net.URI(url).host ?: url } catch (_: Exception) { url }
            } ?: "URL QR"
            QrType.TEXT -> data["text"]?.take(30) ?: "Text QR"
            QrType.WIFI -> data["ssid"]?.let { "WiFi: $it" } ?: "WiFi QR"
            QrType.EMAIL -> data["address"]?.let { "Email: $it" } ?: "Email QR"
            QrType.SMS -> data["phone"]?.let { "SMS to $it" } ?: "SMS QR"
            QrType.PHONE -> data["phone"]?.let { "Call $it" } ?: "Phone QR"
            QrType.WHATSAPP -> data["phone"]?.let { "WhatsApp: $it" } ?: "WhatsApp QR"
            QrType.CONTACT -> data["name"]?.let { "Contact: $it" } ?: "vCard QR"
            QrType.LOCATION -> "Location QR"
            QrType.EVENT -> data["title"]?.let { "Event: $it" } ?: "Event QR"
            QrType.PLAY_STORE -> "Play Store QR"
            QrType.APP_STORE -> "App Store QR"
            QrType.SOCIAL -> "Social QR"
            QrType.CUSTOM -> "Custom QR"
        }
    }

    private fun isFormValidInternal(type: QrType, data: Map<String, String>): Boolean {
        return when (type) {
            QrType.URL -> data["url"]?.isNotBlank() == true
            QrType.TEXT -> data["text"]?.isNotBlank() == true
            QrType.WIFI -> data["ssid"]?.isNotBlank() == true
            QrType.EMAIL -> data["address"]?.isNotBlank() == true && data["address"]?.contains("@") == true
            QrType.SMS -> data["phone"]?.isNotBlank() == true
            QrType.PHONE -> data["phone"]?.isNotBlank() == true
            QrType.WHATSAPP -> data["phone"]?.isNotBlank() == true
            QrType.CONTACT -> data["name"]?.isNotBlank() == true
            QrType.LOCATION -> data["latitude"]?.isNotBlank() == true && data["longitude"]?.isNotBlank() == true
            QrType.EVENT -> data["title"]?.isNotBlank() == true
            QrType.PLAY_STORE -> data["package"]?.isNotBlank() == true
            QrType.APP_STORE -> data["url"]?.isNotBlank() == true
            QrType.SOCIAL -> data["url"]?.isNotBlank() == true
            QrType.CUSTOM -> data["content"]?.isNotBlank() == true
        }
    }
}
