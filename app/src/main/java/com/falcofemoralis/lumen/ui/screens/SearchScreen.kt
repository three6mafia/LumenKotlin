package com.falcofemoralis.lumen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import com.falcofemoralis.lumen.data.model.FilmCard
import com.falcofemoralis.lumen.ui.components.TvFilmCard
import com.falcofemoralis.lumen.ui.components.TvLazyColumn
import com.falcofemoralis.lumen.ui.components.TvLazyRow
import com.falcofemoralis.lumen.ui.theme.*
import com.falcofemoralis.lumen.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onFilmClick: (FilmCard) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val popularQueries = listOf("Дюна", "Фоллаут", "Дэдпул", "Оппенгеймер", "Атака титанов", "Одни из нас", "Дом Дракона")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("search_screen")
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
                        text = "Поиск фильмов и сериалов",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Search Input Box
            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onQueryChange(it) },
                    placeholder = {
                        Text("Введите название...", color = TextMuted)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Поиск", tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (uiState.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = LumenRed,
                        unfocusedBorderColor = DarkBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("search_input")
                )
            }

            // Quick Query Chips
            item {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text(
                        text = "Популярные запросы",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TvLazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(popularQueries) { q ->
                            TvChip(
                                text = q,
                                isSelected = uiState.query == q,
                                onClick = { viewModel.submitSearch(q) }
                            )
                        }
                    }
                }
            }

            // Loading Indicator
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = LumenRed)
                    }
                }
            }

            // Search Results
            if (!uiState.isLoading && uiState.results.isNotEmpty()) {
                item {
                    Text(
                        text = "Найдено (${uiState.results.size})",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                // Grid chunks of 5 items
                val chunks = uiState.results.chunked(5)
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
                        // Fill extra space if last row has fewer items
                        repeat(5 - rowCount) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else if (!uiState.isLoading && uiState.query.isNotBlank() && uiState.results.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "По запросу \"${uiState.query}\" ничего не найдено",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
