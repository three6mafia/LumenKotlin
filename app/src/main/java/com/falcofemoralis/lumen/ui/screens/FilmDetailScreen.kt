package com.falcofemoralis.lumen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import com.falcofemoralis.lumen.data.model.*
import com.falcofemoralis.lumen.ui.components.TvFilmCard
import com.falcofemoralis.lumen.ui.components.TvLazyColumn
import com.falcofemoralis.lumen.ui.components.TvLazyRow
import com.falcofemoralis.lumen.ui.theme.*
import com.falcofemoralis.lumen.ui.viewmodel.FilmDetailViewModel

@Composable
fun FilmDetailScreen(
    viewModel: FilmDetailViewModel,
    onBack: () -> Unit,
    onPlay: (FilmDetails, FilmVideo?, Episode?) -> Unit,
    onRelatedClick: (FilmCard) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val film = uiState.filmDetails

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("film_detail_screen")
    ) {
        if (uiState.isLoading || film == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LumenRed)
            }
        } else {
            TvLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                // Header Banner & Backdrop
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                    ) {
                        AsyncImage(
                            model = film.largePoster.ifBlank { film.poster },
                            contentDescription = film.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Dark gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x990F0F12),
                                            Color(0xF50F0F12),
                                            DarkBackground
                                        )
                                    )
                                )
                        )

                        // Top Back Button
                        Box(
                            modifier = Modifier
                                .padding(24.dp)
                                .align(Alignment.TopStart)
                        ) {
                            TvActionButton(
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                onClick = onBack
                            )
                        }

                        // Film Main Info Overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Poster Card
                            AsyncImage(
                                model = film.poster,
                                contentDescription = film.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(210.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.width(20.dp))

                            Column {
                                if (film.rating.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "★ ${film.rating}",
                                            color = RatingGreen,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (film.votes.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${film.votes})",
                                                color = TextMuted,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = film.type.title,
                                            color = LumenRed,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                Text(
                                    text = film.title,
                                    color = Color.White,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (!film.originalTitle.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = film.originalTitle,
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                val metadata = listOfNotNull(
                                    film.releaseDate.ifBlank { null },
                                    film.duration.ifBlank { null },
                                    film.countries.firstOrNull()
                                ).joinToString(" • ")

                                if (metadata.isNotBlank()) {
                                    Text(
                                        text = metadata,
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Play & Bookmark Action Buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    TvActionButton(
                                        text = "Смотреть",
                                        icon = Icons.Default.PlayArrow,
                                        isPrimary = true,
                                        onClick = {
                                            onPlay(film, uiState.video, uiState.selectedEpisode)
                                        }
                                    )

                                    TvActionButton(
                                        icon = if (uiState.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        onClick = { viewModel.toggleBookmark() }
                                    )
                                }
                            }
                        }
                    }
                }

                // Description
                if (film.description.isNotBlank()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Описание",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = film.description,
                                color = TextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Voice Translation Shelves
                if (film.voices.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Озвучка",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                            )

                            TvLazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(film.voices) { voice ->
                                    val isSelected = voice.id == uiState.selectedVoice?.id
                                    TvChip(
                                        text = voice.title,
                                        isSelected = isSelected,
                                        onClick = { viewModel.selectVoice(voice) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Seasons and Episodes list (if series)
                uiState.selectedVoice?.seasons?.let { seasons ->
                    if (seasons.isNotEmpty()) {
                        // Season Selector
                        if (seasons.size > 1) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Сезоны",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                                    )

                                    TvLazyRow(
                                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(seasons) { season ->
                                            val isSelected = season.seasonId == uiState.selectedSeason?.seasonId
                                            TvChip(
                                                text = season.name,
                                                isSelected = isSelected,
                                                onClick = { viewModel.selectSeason(season) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Episode List
                        uiState.selectedSeason?.episodes?.let { episodes ->
                            if (episodes.isNotEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Серии",
                                            color = TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                                        )

                                        TvLazyRow(
                                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            items(episodes) { episode ->
                                                val isSelected = episode.episodeId == uiState.selectedEpisode?.episodeId
                                                TvChip(
                                                    text = episode.name,
                                                    isSelected = isSelected,
                                                    onClick = {
                                                        viewModel.selectEpisode(episode)
                                                        onPlay(film, uiState.video, episode)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Related Films Shelf
                if (film.related.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Похожие",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                            )

                            TvLazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(film.related) { relatedCard ->
                                    TvFilmCard(
                                        film = relatedCard,
                                        onClick = { onRelatedClick(relatedCard) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TvChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFocused -> FocusGold
                    isSelected -> LumenRed
                    else -> DarkSurfaceVariant
                }
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FocusGold else if (isSelected) LumenRedLight else DarkBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isFocused) Color.Black else Color.White,
            fontSize = 13.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal
        )
    }
}
