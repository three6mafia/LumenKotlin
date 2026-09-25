package com.falcofemoralis.lumen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falcofemoralis.lumen.data.model.CategorySection
import com.falcofemoralis.lumen.data.model.FilmCard
import com.falcofemoralis.lumen.data.repository.RezkaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val heroFilm: FilmCard? = null,
    val sections: List<CategorySection> = emptyList(),
    val favorites: List<FilmCard> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedTab: Int = 0
)

class HomeViewModel(
    private val repository: RezkaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val sections = repository.getHomeSections()
                val favorites = repository.getBookmarks()
                val firstHero = sections.firstOrNull { it.films.isNotEmpty() }?.films?.firstOrNull()

                _uiState.update {
                    it.copy(
                        sections = sections,
                        heroFilm = firstHero,
                        favorites = favorites,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Ошибка загрузки"
                    )
                }
            }
        }
    }

    fun selectHeroFilm(film: FilmCard) {
        _uiState.update { it.copy(heroFilm = film) }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        if (index == 2) {
            // Refresh favorites
            _uiState.update { it.copy(favorites = repository.getBookmarks()) }
        }
    }

    fun toggleBookmark(film: FilmCard) {
        repository.toggleBookmark(film)
        _uiState.update { it.copy(favorites = repository.getBookmarks()) }
    }
}
