package com.qrforge.ui.screens.create

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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

data class ResultUiState(
    val title: String = "",
    val type: String = "",
    val content: String = "",
    val qrBitmap: Bitmap? = null,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val historyRepository: QrHistoryRepository,
    private val qrGenerator: QrGenerator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val historyId: Long = savedStateHandle.get<Long>("historyId") ?: 0L

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        loadItem()
    }

    private fun loadItem() {
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
                        qrBitmap = bitmap,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "QR code not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to generate QR"
                )
            }
        }
    }

    fun exportPng() = exportFile("png") { file ->
        _uiState.value.qrBitmap?.let { bitmap ->
            qrGenerator.exportPng(bitmap, file)
        }
    }

    fun exportSvg() { exportPng() }

    fun exportPdf() { exportPng() }

    fun sharePng() {
        viewModelScope.launch {
            try {
                val bitmap = _uiState.value.qrBitmap ?: return@launch
                val file = createTempFile("qr_share.png")
                withContext(Dispatchers.IO) {
                    qrGenerator.exportPng(bitmap, file)
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(shareIntent, "Share QR Code").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    exportMessage = "Share failed: ${e.message}"
                )
            }
        }
    }

    private fun exportFile(extension: String, writer: suspend (File) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportMessage = null)
            try {
                val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveToMediaStore(extension, writer)
                } else {
                    saveToDownloads(extension, writer)
                }
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportMessage = "Saved: ${file.name}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportMessage = "Export failed: ${e.message}"
                )
            }
        }
    }

    private suspend fun saveToMediaStore(extension: String, writer: suspend (File) -> Unit): File {
        return withContext(Dispatchers.IO) {
            val mimeType = when (extension) {
                "png" -> "image/png"
                "svg" -> "image/svg+xml"
                "pdf" -> "application/pdf"
                else -> "application/octet-stream"
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "QR_${_uiState.value.title}_${System.currentTimeMillis()}.$extension")
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QRForge")
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: throw Exception("Failed to create MediaStore entry")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val tempFile = createTempFile("temp.$extension")
                writer(tempFile)
                outputStream.write(tempFile.readBytes())
                tempFile.delete()
            } ?: throw Exception("Failed to open output stream")

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(File(downloadsDir, "QRForge"), contentValues.getAsString(MediaStore.Downloads.DISPLAY_NAME))
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun saveToDownloads(extension: String, writer: suspend (File) -> Unit): File {
        return withContext(Dispatchers.IO) {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "QRForge"
            )
            dir.mkdirs()
            val file = File(dir, "QR_${_uiState.value.title}_${System.currentTimeMillis()}.$extension")
            writer(file)
            file
        }
    }

    private fun createTempFile(name: String): File {
        val dir = File(context.cacheDir, "qr_exports")
        dir.mkdirs()
        return File(dir, name)
    }

    fun clearExportMessage() {
        _uiState.value = _uiState.value.copy(exportMessage = null)
    }
}
