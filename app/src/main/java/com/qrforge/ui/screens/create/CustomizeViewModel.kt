package com.qrforge.ui.screens.create

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrforge.domain.model.CustomizationOptions
import com.qrforge.domain.model.DotStyle
import com.qrforge.domain.model.EyeShape
import com.qrforge.domain.model.FrameStyle
import com.qrforge.repository.QrHistoryRepository
import com.qrforge.util.QrGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CustomizeUiState(
    val content: String = "",
    val title: String = "",
    val type: String = "",
    val qrBitmap: Bitmap? = null,
    val fgColor: Long = 0xFF090909,
    val bgColor: Long = 0xFFFFFFFF,
    val dotStyle: DotStyle = DotStyle.ROUNDED,
    val eyeShape: EyeShape = EyeShape.MODERN,
    val frameStyle: FrameStyle = FrameStyle.NONE,
    val logoPath: String? = null,
    val gradientType: String = "NONE",
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CustomizeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val historyRepository: QrHistoryRepository,
    private val qrGenerator: QrGenerator
) : ViewModel() {

    private val historyId: Long = savedStateHandle.get<Long>("historyId") ?: 0L

    private val _uiState = MutableStateFlow(CustomizeUiState())
    val uiState: StateFlow<CustomizeUiState> = _uiState.asStateFlow()

    init {
        loadItem()
    }

    private fun loadItem() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val item = historyRepository.getByIdOnce(historyId)
                if (item != null) {
                    _uiState.value = _uiState.value.copy(
                        content = item.content,
                        title = item.title,
                        type = item.type,
                        fgColor = item.foregroundColor,
                        bgColor = item.backgroundColor,
                        dotStyle = DotStyle.entries.find { it.name.equals(item.dotStyle, ignoreCase = true) } ?: DotStyle.ROUNDED,
                        eyeShape = EyeShape.entries.find { it.name.equals(item.eyeShape, ignoreCase = true) } ?: EyeShape.MODERN,
                        frameStyle = FrameStyle.entries.find { it.name.equals(item.frameStyle, ignoreCase = true) } ?: FrameStyle.NONE,
                        logoPath = item.logoPath,
                        isLoading = false
                    )
                    generatePreview()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "QR code data not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load data"
                )
            }
        }
    }

    fun updateFgColor(color: Long) {
        _uiState.value = _uiState.value.copy(fgColor = color)
        generatePreview()
    }

    fun updateBgColor(color: Long) {
        _uiState.value = _uiState.value.copy(bgColor = color)
        generatePreview()
    }

    fun updateDotStyle(style: DotStyle) {
        _uiState.value = _uiState.value.copy(dotStyle = style)
        generatePreview()
    }

    fun updateEyeShape(shape: EyeShape) {
        _uiState.value = _uiState.value.copy(eyeShape = shape)
        generatePreview()
    }

    fun updateFrameStyle(style: FrameStyle) {
        _uiState.value = _uiState.value.copy(frameStyle = style)
        generatePreview()
    }

    fun updateLogoPath(path: String?) {
        _uiState.value = _uiState.value.copy(logoPath = path)
        generatePreview()
    }

    fun updateGradientType(type: String) {
        _uiState.value = _uiState.value.copy(gradientType = type)
        generatePreview()
    }

    private fun generatePreview() {
        val state = _uiState.value
        if (state.content.isBlank()) return

        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.Default) {
                    qrGenerator.generateBitmap(
                        content = state.content,
                        fgColor = state.fgColor.toInt(),
                        bgColor = state.bgColor.toInt(),
                        dotStyle = state.dotStyle.name,
                        eyeShape = state.eyeShape.name,
                        frameStyle = state.frameStyle.name,
                        logoPath = state.logoPath,
                        gradientType = state.gradientType,
                        size = 512
                    )
                }
                _uiState.value = _uiState.value.copy(qrBitmap = bitmap, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Preview generation failed: ${e.message}"
                )
            }
        }
    }

    fun generateAndSave(onGenerated: (Long) -> Unit) {
        val state = _uiState.value
        if (state.isGenerating || state.content.isBlank()) return

        viewModelScope.launch {
            _uiState.value = state.copy(isGenerating = true)
            try {
                val item = historyRepository.getByIdOnce(historyId)
                if (item != null) {
                    val updated = item.copy(
                        foregroundColor = state.fgColor,
                        backgroundColor = state.bgColor,
                        dotStyle = state.dotStyle.name,
                        eyeShape = state.eyeShape.name,
                        frameStyle = state.frameStyle.name,
                        logoPath = state.logoPath
                    )
                    historyRepository.update(updated)
                }
                onGenerated(historyId)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isGenerating = false,
                    error = e.message ?: "Failed to save customization"
                )
            }
        }
    }
}
