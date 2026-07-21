package com.qrforge.ui.screens.detail

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrforge.repository.QrHistoryRepository
import com.qrforge.util.QrGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class DetailUiState(
    val title: String = "",
    val type: String = "",
    val content: String = "",
    val rawData: String = "",
    val qrBitmap: Bitmap? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = 0L,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val error: String? = null,
    val showDeleteConfirm: Boolean = false
)

@HiltViewModel
class QrDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val historyRepository: QrHistoryRepository,
    private val qrGenerator: QrGenerator,
    @ApplicationContext private val appContext: android.content.Context
) : ViewModel() {

    private val historyId: Long = savedStateHandle.get<Long>("historyId") ?: 0L

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadItem()
    }

    fun loadItem() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val item = historyRepository.getByIdOnce(historyId)
                if (item != null) {
                    val bitmap = withContext(Dispatchers.Default) {
                        qrGenerator.generateBitmap(
                            content = item.content,
                            fgColor = item.foregroundColor.toInt(),
                            bgColor = item.backgroundColor.toInt(),
                            dotStyle = item.dotStyle,
                            eyeShape = item.eyeShape,
                            frameStyle = item.frameStyle,
                            logoPath = item.logoPath
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        title = item.title,
                        type = item.type,
                        content = item.content,
                        rawData = item.rawData,
                        qrBitmap = bitmap,
                        isFavorite = item.isFavorite,
                        createdAt = item.createdAt,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "QR code not found")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Load failed")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val current = _uiState.value.isFavorite
            historyRepository.toggleFavorite(historyId, !current)
            _uiState.value = _uiState.value.copy(isFavorite = !current)
        }
    }

    fun rename(newTitle: String) {
        viewModelScope.launch {
            historyRepository.rename(historyId, newTitle)
            _uiState.value = _uiState.value.copy(title = newTitle)
        }
    }

    fun delete() {
        viewModelScope.launch {
            historyRepository.deleteById(historyId)
        }
    }

    fun showDeleteConfirm() { _uiState.value = _uiState.value.copy(showDeleteConfirm = true) }
    fun dismissDeleteConfirm() { _uiState.value = _uiState.value.copy(showDeleteConfirm = false) }

    fun sharePng() {
        viewModelScope.launch {
            try {
                val bitmap = _uiState.value.qrBitmap ?: return@launch
                val dir = File(appContext.cacheDir, "qr_exports")
                dir.mkdirs()
                val file = File(dir, "qr_share.png")
                withContext(Dispatchers.IO) { qrGenerator.exportPng(bitmap, file) }
                val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                appContext.startActivity(Intent.createChooser(intent, "Share QR Code").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(exportMessage = "Share failed: ${e.message}")
            }
        }
    }

    fun clearExportMessage() { _uiState.value = _uiState.value.copy(exportMessage = null) }

    fun downloadPng() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val bitmap = _uiState.value.qrBitmap ?: return@launch
                val file = withContext(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val mimeType = "image/png"
                        val values = ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, "QR_${_uiState.value.title}_${System.currentTimeMillis()}.png")
                            put(MediaStore.Downloads.MIME_TYPE, mimeType)
                            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QRForge")
                        }
                        val uri = appContext.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                            ?: throw Exception("Failed to create MediaStore entry")
                        appContext.contentResolver.openOutputStream(uri)?.use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        File(File(downloadsDir, "QRForge"), values.getAsString(MediaStore.Downloads.DISPLAY_NAME))
                    } else {
                        @Suppress("DEPRECATION")
                        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "QRForge")
                        dir.mkdirs()
                        val outFile = File(dir, "QR_${_uiState.value.title}_${System.currentTimeMillis()}.png")
                        java.io.FileOutputStream(outFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        outFile
                    }
                }
                _uiState.value = _uiState.value.copy(isExporting = false, exportMessage = "Saved: ${file.name}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, exportMessage = "Download failed: ${e.message}")
            }
        }
    }
}
