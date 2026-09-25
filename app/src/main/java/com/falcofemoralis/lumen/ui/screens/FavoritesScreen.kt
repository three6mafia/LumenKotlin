package com.falcofemoralis.lumen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import com.falcofemoralis.lumen.data.model.FilmCard
import com.falcofemoralis.lumen.ui.components.TvFilmCard
import com.falcofemoralis.lumen.ui.components.TvLazyColumn
import com.falcofemoralis.lumen.ui.theme.DarkBackground
import com.falcofemoralis.lumen.ui.theme.TextSecondary
import com.falcofemoralis.lumen.ui.viewmodel.HomeViewModel

@Composable
fun FavoritesScreen(
    viewModel: HomeViewModel,
    onFilmClick: (FilmCard) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val favorites = uiState.favorites

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("favorites_screen")
    ) {
        TvLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp)
        ) {
            // Header Bar
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    TvActionButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = onBack
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Закладки и избранное (${favorites.size})",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (favorites.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "У вас пока нет сохраненных фильмов в закладках",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                val chunks = favorites.chunked(5)
                items(chunks) { rowFilms ->
                    val rowCount = rowFilms.size
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        rowFilms.forEach { film ->
                            TvFilmCard(
                                film = film,
                                onClick = { onFilmClick(film) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(5 - rowCount) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
