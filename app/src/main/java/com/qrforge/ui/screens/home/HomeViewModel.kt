package com.qrforge.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrforge.domain.model.QrHistoryItem
import com.qrforge.domain.model.Template
import com.qrforge.repository.QrHistoryRepository
import com.qrforge.repository.QrTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recentHistory: List<QrHistoryItem> = emptyList(),
    val templates: List<Template> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val historyRepository: QrHistoryRepository,
    private val templateRepository: QrTemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            templateRepository.seedTemplates()
            combine(
                historyRepository.getRecentHistory(5),
                templateRepository.getAllTemplates()
            ) { history, templates ->
                HomeUiState(recentHistory = history, templates = templates.take(5), isLoading = false)
            }.collect { _uiState.value = it }
        }
    }

    fun toggleFavorite(id: Long, current: Boolean) {
        viewModelScope.launch { historyRepository.toggleFavorite(id, !current) }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch { historyRepository.deleteById(id) }
    }
}
