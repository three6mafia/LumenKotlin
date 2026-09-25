package com.falcofemoralis.lumen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falcofemoralis.lumen.data.model.*
import com.falcofemoralis.lumen.data.repository.RezkaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FilmDetailUiState(
    val filmDetails: FilmDetails? = null,
    val selectedVoice: FilmVoice? = null,
    val selectedSeason: Season? = null,
    val selectedEpisode: Episode? = null,
    val video: FilmVideo? = null,
    val isLoading: Boolean = false,
    val isStreamsLoading: Boolean = false,
    val isBookmarked: Boolean = false,
    val error: String? = null
)

class FilmDetailViewModel(
    private val repository: RezkaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilmDetailUiState())
    val uiState: StateFlow<FilmDetailUiState> = _uiState.asStateFlow()

    fun loadFilm(link: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val details = repository.getFilmDetails(link)
                val initialVoice = details.voices.firstOrNull { it.isActive } ?: details.voices.firstOrNull()

                // Check if voice has seasons or if we need to load them
                val seasons = if (initialVoice != null && initialVoice.seasons.isNotEmpty()) {
                    initialVoice.seasons
                } else if (initialVoice != null && details.type != FilmType.FILM) {
                    repository.getSeasons(details.id, initialVoice.id)
                } else emptyList()

                val updatedVoice = initialVoice?.copy(seasons = seasons)
                val initialSeason = seasons.firstOrNull()
                val initialEpisode = initialSeason?.episodes?.firstOrNull()

                _uiState.update {
                    it.copy(
                        filmDetails = details,
                        selectedVoice = updatedVoice,
                        selectedSeason = initialSeason,
                        selectedEpisode = initialEpisode,
                        isBookmarked = repository.isBookmarked(details.id),
                        isLoading = false
                    )
                }

                loadStreams()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Ошибка загрузки информации о фильме"
                    )
                }
            }
        }
    }

    fun selectVoice(voice: FilmVoice) {
        viewModelScope.launch {
            val details = _uiState.value.filmDetails ?: return@launch
            val seasons = if (voice.seasons.isNotEmpty()) {
                voice.seasons
            } else if (details.type != FilmType.FILM) {
                repository.getSeasons(details.id, voice.id)
            } else emptyList()

            val updatedVoice = voice.copy(seasons = seasons)
            val season = seasons.firstOrNull()
            val episode = season?.episodes?.firstOrNull()

            _uiState.update {
                it.copy(
                    selectedVoice = updatedVoice,
                    selectedSeason = season,
                    selectedEpisode = episode
                )
            }

            loadStreams()
        }
    }

    fun selectSeason(season: Season) {
        val firstEp = season.episodes.firstOrNull()
        _uiState.update {
            it.copy(
                selectedSeason = season,
                selectedEpisode = firstEp
            )
        }
        loadStreams()
    }

    fun selectEpisode(episode: Episode) {
        _uiState.update { it.copy(selectedEpisode = episode) }
        loadStreams()
    }

    fun toggleBookmark() {
        val details = _uiState.value.filmDetails ?: return
        val card = FilmCard(
            id = details.id,
            link = details.link,
            type = details.type,
            poster = details.poster,
            title = details.title,
            rating = details.rating
        )
        val newState = repository.toggleBookmark(card)
        _uiState.update { it.copy(isBookmarked = newState) }
    }

    private fun loadStreams() {
        viewModelScope.launch {
            val details = _uiState.value.filmDetails ?: return@launch
            val voice = _uiState.value.selectedVoice
            val season = _uiState.value.selectedSeason
            val episode = _uiState.value.selectedEpisode

            _uiState.update { it.copy(isStreamsLoading = true) }

            // If voice already has inline video, use it
            if (voice?.video != null && voice.video.streams.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        video = voice.video,
                        isStreamsLoading = false
                    )
                }
                return@launch
            }

            val video = repository.getStreams(
                filmId = details.id,
                voiceId = voice?.id ?: "",
                isCamrip = voice?.isCamrip ?: "0",
                isAds = voice?.isAds ?: "0",
                isDirector = voice?.isDirector ?: "0",
                seasonId = season?.seasonId,
                episodeId = episode?.episodeId
            )

            _uiState.update {
                it.copy(
                    video = video,
                    isStreamsLoading = false
                )
            }
        }
    }
}
