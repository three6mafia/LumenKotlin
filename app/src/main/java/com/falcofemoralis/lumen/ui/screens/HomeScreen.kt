package com.falcofemoralis.lumen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import com.falcofemoralis.lumen.data.model.FilmCard
import com.falcofemoralis.lumen.ui.components.TvCategoryRow
import com.falcofemoralis.lumen.ui.components.TvLazyColumn
import com.falcofemoralis.lumen.ui.theme.*
import com.falcofemoralis.lumen.ui.viewmodel.HomeUiState
import com.falcofemoralis.lumen.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onFilmClick: (FilmCard) -> Unit,
    onPlayFilm: (FilmCard) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("home_screen")
    ) {
        if (uiState.isLoading && uiState.sections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LumenRed)
            }
        } else {
            // TV Lazy Column for full vertical remote scrolling
            TvLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp)
            ) {
                // Top Header & Navigation Bar
                item {
                    HomeTopBar(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = { tabIdx ->
                            when (tabIdx) {
                                1 -> onOpenSearch()
                                2 -> onOpenFavorites()
                                else -> viewModel.setTab(tabIdx)
                            }
                        }
                    )
                }

                // Hero Billboard Banner
                item {
                    uiState.heroFilm?.let { hero ->
                        HeroBillboard(
                            film = hero,
                            isBookmarked = uiState.favorites.any { it.id == hero.id },
                            onPlay = { onPlayFilm(hero) },
                            onDetails = { onFilmClick(hero) },
                            onToggleBookmark = { viewModel.toggleBookmark(hero) }
                        )
                    }
                }

                // Category Shelves
                items(uiState.sections, key = { it.id }) { section ->
                    TvCategoryRow(
                        section = section,
                        onFilmClick = { film ->
                            viewModel.selectHeroFilm(film)
                            onFilmClick(film)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Главная", "Поиск", "Закладки")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // App Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(LumenRed)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LUMEN",
                color = LumenRed,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        }

        // Navigation Tabs
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedTab
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isFocused -> LumenRed
                                isSelected -> DarkSurfaceVariant
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isFocused) FocusGold else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .focusable(interactionSource = interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onTabSelected(index) }
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        color = if (isSelected || isFocused) Color.White else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBillboard(
    film: FilmCard,
    isBookmarked: Boolean,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
    ) {
        // Background Backdrop Image
        AsyncImage(
            model = film.poster,
            contentDescription = film.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xF50F0F12),
                            Color(0xCC0F0F12),
                            Color(0x400F0F12)
                        )
                    )
                )
        )

        // Hero Info
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(24.dp)
                .fillMaxWidth(0.6f)
        ) {
            if (film.rating.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★ ${film.rating}",
                        color = RatingGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = film.type.title,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = film.title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val subtitleText = film.subtitle.ifBlank { film.info }
            if (subtitleText.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitleText,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TvActionButton(
                    text = "Смотреть",
                    icon = Icons.Default.PlayArrow,
                    isPrimary = true,
                    onClick = onPlay
                )

                TvActionButton(
                    text = "Подробнее",
                    onClick = onDetails
                )

                TvActionButton(
                    icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    onClick = onToggleBookmark
                )
            }
        }
    }
}

@Composable
fun TvActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isPrimary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFocused -> FocusGold
                    isPrimary -> LumenRed
                    else -> Color(0x662A2A35)
                }
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FocusGold else Color(0x44FFFFFF),
                shape = RoundedCornerShape(8.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = if (isFocused) Color.Black else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (text != null) {
                if (icon != null) Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = text,
                    color = if (isFocused) Color.Black else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
