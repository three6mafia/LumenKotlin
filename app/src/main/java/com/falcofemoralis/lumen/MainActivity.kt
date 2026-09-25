package com.falcofemoralis.lumen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.falcofemoralis.lumen.data.model.Episode
import com.falcofemoralis.lumen.data.model.FilmCard
import com.falcofemoralis.lumen.data.model.FilmDetails
import com.falcofemoralis.lumen.data.model.FilmStream
import com.falcofemoralis.lumen.data.model.FilmVideo
import com.falcofemoralis.lumen.data.repository.RezkaRepository
import com.falcofemoralis.lumen.ui.components.PlayerComposable
import com.falcofemoralis.lumen.ui.screens.FavoritesScreen
import com.falcofemoralis.lumen.ui.screens.FilmDetailScreen
import com.falcofemoralis.lumen.ui.screens.HomeScreen
import com.falcofemoralis.lumen.ui.screens.SearchScreen
import com.falcofemoralis.lumen.ui.theme.DarkBackground
import com.falcofemoralis.lumen.ui.theme.LumenTheme
import com.falcofemoralis.lumen.ui.viewmodel.FilmDetailViewModel
import com.falcofemoralis.lumen.ui.viewmodel.HomeViewModel
import com.falcofemoralis.lumen.ui.viewmodel.SearchViewModel

sealed interface AppScreen {
    data object Home : AppScreen
    data object Search : AppScreen
    data object Favorites : AppScreen
    data class Detail(val link: String, val title: String) : AppScreen
    data class Player(
        val title: String,
        val subtitle: String? = null,
        val streams: List<FilmStream>
    ) : AppScreen
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = RezkaRepository(applicationContext)

        setContent {
            LumenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val homeViewModel = remember { HomeViewModel(repository) }
                    val detailViewModel = remember { FilmDetailViewModel(repository) }
                    val searchViewModel = remember { SearchViewModel(repository) }

                    var screenStack by remember { mutableStateOf(listOf<AppScreen>(AppScreen.Home)) }
                    val currentScreen = screenStack.last()

                    fun navigateTo(screen: AppScreen) {
                        screenStack = screenStack + screen
                    }

                    fun navigateBack() {
                        if (screenStack.size > 1) {
                            screenStack = screenStack.dropLast(1)
                        }
                    }

                    BackHandler(enabled = screenStack.size > 1) {
                        navigateBack()
                    }

                    when (val screen = currentScreen) {
                        is AppScreen.Home -> {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onFilmClick = { film ->
                                    detailViewModel.loadFilm(film.link)
                                    navigateTo(AppScreen.Detail(film.link, film.title))
                                },
                                onPlayFilm = { film ->
                                    detailViewModel.loadFilm(film.link)
                                    // Start with default streams or navigate to details
                                    val fallbackStreams = listOf(
                                        FilmStream("1080p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                                        FilmStream("720p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
                                    )
                                    navigateTo(
                                        AppScreen.Player(
                                            title = film.title,
                                            subtitle = film.subtitle.ifBlank { null },
                                            streams = fallbackStreams
                                        )
                                    )
                                },
                                onOpenSearch = { navigateTo(AppScreen.Search) },
                                onOpenFavorites = { navigateTo(AppScreen.Favorites) }
                            )
                        }

                        is AppScreen.Search -> {
                            SearchScreen(
                                viewModel = searchViewModel,
                                onFilmClick = { film ->
                                    detailViewModel.loadFilm(film.link)
                                    navigateTo(AppScreen.Detail(film.link, film.title))
                                },
                                onBack = { navigateBack() }
                            )
                        }

                        is AppScreen.Favorites -> {
                            FavoritesScreen(
                                viewModel = homeViewModel,
                                onFilmClick = { film ->
                                    detailViewModel.loadFilm(film.link)
                                    navigateTo(AppScreen.Detail(film.link, film.title))
                                },
                                onBack = { navigateBack() }
                            )
                        }

                        is AppScreen.Detail -> {
                            FilmDetailScreen(
                                viewModel = detailViewModel,
                                onBack = { navigateBack() },
                                onPlay = { filmDetails, video, episode ->
                                    val streams = video?.streams?.ifEmpty { null } ?: listOf(
                                        FilmStream("1080p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                                        FilmStream("720p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                                        FilmStream("480p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
                                    )
                                    val epSubtitle = episode?.name ?: filmDetails.originalTitle
                                    navigateTo(
                                        AppScreen.Player(
                                            title = filmDetails.title,
                                            subtitle = epSubtitle,
                                            streams = streams
                                        )
                                    )
                                },
                                onRelatedClick = { relatedCard ->
                                    detailViewModel.loadFilm(relatedCard.link)
                                    navigateTo(AppScreen.Detail(relatedCard.link, relatedCard.title))
                                }
                            )
                        }

                        is AppScreen.Player -> {
                            PlayerComposable(
                                title = screen.title,
                                subtitle = screen.subtitle,
                                streams = screen.streams,
                                onBack = { navigateBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
