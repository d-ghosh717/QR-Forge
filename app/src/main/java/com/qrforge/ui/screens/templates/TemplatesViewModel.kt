package com.qrforge.ui.screens.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrforge.domain.model.Template
import com.qrforge.repository.QrTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TemplatesUiState(
    val templates: List<Template> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val isLoading: Boolean = true
)

data class TemplatePreviewUiState(
    val template: Template? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val repository: QrTemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplatesUiState())
    val uiState: StateFlow<TemplatesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedTemplates()
            repository.getCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = listOf("All") + categories)
            }
        }
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            val flow = if (_uiState.value.selectedCategory == "All") {
                repository.getAllTemplates()
            } else {
                repository.getTemplatesByCategory(_uiState.value.selectedCategory)
            }
            flow.collect { templates -> _uiState.value = _uiState.value.copy(templates = templates, isLoading = false) }
        }
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category, isLoading = true)
        loadTemplates()
    }

    suspend fun getTemplateById(id: Long): Template? = repository.getById(id)
}
