package com.qrforge.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrforge.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val exportQuality: String = "High",
    val defaultFgColor: String = "#090909",
    val defaultBgColor: String = "#FFFFFF",
    val defaultDotStyle: String = "Rounded",
    val defaultEyeShape: String = "Modern",
    val appVersion: String = "1.0.0"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { loadSettings() }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                exportQuality = settingsRepository.getString(SettingsRepository.KEY_EXPORT_QUALITY, "High"),
                defaultFgColor = settingsRepository.getString(SettingsRepository.KEY_DEFAULT_FG_COLOR, "#090909"),
                defaultBgColor = settingsRepository.getString(SettingsRepository.KEY_DEFAULT_BG_COLOR, "#FFFFFF"),
                defaultDotStyle = settingsRepository.getString(SettingsRepository.KEY_DEFAULT_DOT_STYLE, "Rounded"),
                defaultEyeShape = settingsRepository.getString(SettingsRepository.KEY_DEFAULT_EYE_SHAPE, "Modern")
            )
        }
    }

    fun setExportQuality(quality: String) {
        _uiState.value = _uiState.value.copy(exportQuality = quality)
        viewModelScope.launch { settingsRepository.setString(SettingsRepository.KEY_EXPORT_QUALITY, quality) }
    }

    fun setDefaultFgColor(color: String) {
        _uiState.value = _uiState.value.copy(defaultFgColor = color)
        viewModelScope.launch { settingsRepository.setString(SettingsRepository.KEY_DEFAULT_FG_COLOR, color) }
    }

    fun setDefaultBgColor(color: String) {
        _uiState.value = _uiState.value.copy(defaultBgColor = color)
        viewModelScope.launch { settingsRepository.setString(SettingsRepository.KEY_DEFAULT_BG_COLOR, color) }
    }

    fun setDefaultDotStyle(style: String) {
        _uiState.value = _uiState.value.copy(defaultDotStyle = style)
        viewModelScope.launch { settingsRepository.setString(SettingsRepository.KEY_DEFAULT_DOT_STYLE, style) }
    }

    fun setDefaultEyeShape(shape: String) {
        _uiState.value = _uiState.value.copy(defaultEyeShape = shape)
        viewModelScope.launch { settingsRepository.setString(SettingsRepository.KEY_DEFAULT_EYE_SHAPE, shape) }
    }
}
