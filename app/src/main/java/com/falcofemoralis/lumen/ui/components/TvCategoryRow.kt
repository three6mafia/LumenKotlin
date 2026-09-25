package com.falcofemoralis.lumen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import com.falcofemoralis.lumen.data.model.CategorySection
import com.falcofemoralis.lumen.data.model.FilmCard
import com.falcofemoralis.lumen.ui.theme.LumenRed
import com.falcofemoralis.lumen.ui.theme.TextPrimary

@Composable
fun TvCategoryRow(
    section: CategorySection,
    onFilmClick: (FilmCard) -> Unit,
    modifier: Modifier = Modifier
) {
    if (section.films.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(LumenRed)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = section.title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        TvLazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(section.films, key = { it.id + it.link }) { film ->
                TvFilmCard(
                    film = film,
                    onClick = { onFilmClick(film) }
                )
            }
        }
    }
}
