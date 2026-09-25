package com.falcofemoralis.lumen.data.model

enum class FilmType(val title: String) {
    FILM("Фильм"),
    SERIES("Сериал"),
    CARTOON("Мультфильм"),
    ANIME("Аниме"),
    TV_SHOW("Телешоу")
}

data class FilmCard(
    val id: String = "",
    val link: String = "",
    val type: FilmType = FilmType.FILM,
    val poster: String = "",
    val title: String = "",
    val subtitle: String = "",
    val info: String = "",
    val rating: String = "",
    val isPendingRelease: Boolean = false
)

data class FilmVoice(
    val id: String = "",
    val identifier: String = "",
    val title: String = "",
    val img: String? = null,
    val isCamrip: String = "0",
    val isDirector: String = "0",
    val isAds: String = "0",
    val isActive: Boolean = false,
    val isPremium: Boolean = false,
    val seasons: List<Season> = emptyList(),
    val video: FilmVideo? = null
)

data class Season(
    val seasonId: String,
    val name: String,
    val episodes: List<Episode> = emptyList(),
    val isOnlyEpisodes: Boolean = false
)

data class Episode(
    val episodeId: String,
    val name: String
)

data class FilmStream(
    val quality: String,
    val url: String
)

data class Subtitle(
    val name: String,
    val languageCode: String = "",
    val url: String,
    val isDefault: Boolean = false
)

data class FilmVideo(
    val streams: List<FilmStream> = emptyList(),
    val subtitles: List<Subtitle> = emptyList(),
    val storyboardUrl: String? = null
)

data class ScheduleItem(
    val id: String = "",
    val name: String = "",
    val episodeName: String = "",
    val date: String = "",
    val isWatched: Boolean = false,
    val isReleased: Boolean = false
)

data class ScheduleBlock(
    val name: String = "",
    val items: List<ScheduleItem> = emptyList()
)

data class FilmDetails(
    val id: String = "",
    val link: String = "",
    val type: FilmType = FilmType.FILM,
    val title: String = "",
    val originalTitle: String? = null,
    val poster: String = "",
    val largePoster: String = "",
    val description: String = "",
    val releaseDate: String = "",
    val duration: String = "",
    val rating: String = "",
    val votes: String = "",
    val genres: List<String> = emptyList(),
    val countries: List<String> = emptyList(),
    val directors: List<String> = emptyList(),
    val actors: List<String> = emptyList(),
    val voices: List<FilmVoice> = emptyList(),
    val schedule: List<ScheduleBlock> = emptyList(),
    val related: List<FilmCard> = emptyList(),
    val isPendingRelease: Boolean = false,
    val isBookmarked: Boolean = false
)

data class CategorySection(
    val id: String,
    val title: String,
    val path: String,
    val filterParam: String? = null,
    val films: List<FilmCard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class RecentItem(
    val id: String,
    val link: String,
    val title: String,
    val image: String,
    val date: String,
    val info: String,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val seasonId: String? = null,
    val episodeId: String? = null,
    val voiceId: String? = null
)
