package com.qrforge.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrforge.domain.model.QrHistoryItem
import com.qrforge.repository.QrHistoryRepository
import com.qrforge.util.QrContentEncoder
import com.qrforge.domain.model.QrType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val items: List<QrHistoryItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val sortOrder: SortOrder = SortOrder.RECENT
)

enum class SortOrder(val displayName: String) {
    RECENT("Most Recent"),
    NAME("Name"),
    TYPE("Type")
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: QrHistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.RECENT)
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_searchQuery, _sortOrder) { query, sort -> Pair(query, sort) }
                .flatMapLatest { (query, sort) ->
                    val flow = if (query.isBlank()) historyRepository.getAllHistory()
                    else historyRepository.searchHistory(query)
                    flow.map { items ->
                        when (sort) {
                            SortOrder.RECENT -> items.sortedByDescending { it.updatedAt }
                            SortOrder.NAME -> items.sortedBy { it.title.lowercase() }
                            SortOrder.TYPE -> items.sortedBy { it.type }
                        }
                    }
                }
                .collect { _uiState.value = _uiState.value.copy(items = it, isLoading = false) }
        }
    }

    fun onSearch(query: String) { _searchQuery.value = query }

    fun onSortChange(order: SortOrder) { _sortOrder.value = order }

    fun toggleFavorite(id: Long, current: Boolean) {
        viewModelScope.launch { historyRepository.toggleFavorite(id, !current) }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch { historyRepository.deleteById(id) }
    }

    fun rename(id: Long, newTitle: String) {
        viewModelScope.launch { historyRepository.rename(id, newTitle) }
    }

    fun duplicateItem(id: Long) {
        viewModelScope.launch {
            val original = historyRepository.getByIdOnce(id) ?: return@launch
            val duplicate = original.copy(
                id = 0,
                title = "${original.title} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            historyRepository.insert(duplicate)
        }
    }
}
