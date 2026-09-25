package com.falcofemoralis.lumen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falcofemoralis.lumen.data.model.FilmCard
import com.falcofemoralis.lumen.data.repository.RezkaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val suggestions: List<String> = emptyList(),
    val results: List<FilmCard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchViewModel(
    private val repository: RezkaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), suggestions = emptyList(), isLoading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // Debounce
            _uiState.update { it.copy(isLoading = true) }
            try {
                val results = repository.searchFilms(newQuery)
                val suggestions = repository.searchSuggestions(newQuery)
                _uiState.update {
                    it.copy(
                        results = results,
                        suggestions = suggestions,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage
                    )
                }
            }
        }
    }

    fun submitSearch(query: String) {
        _uiState.update { it.copy(query = query, isLoading = true) }
        viewModelScope.launch {
            try {
                val results = repository.searchFilms(query)
                _uiState.update {
                    it.copy(
                        results = results,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage
                    )
                }
            }
        }
    }
}
