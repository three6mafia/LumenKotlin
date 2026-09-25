package com.falcofemoralis.lumen.data.api

import com.falcofemoralis.lumen.data.model.*
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object RezkaParser {

    fun parseFilmType(urlOrClass: String): FilmType {
        return when {
            urlOrClass.contains("films") -> FilmType.FILM
            urlOrClass.contains("series") -> FilmType.SERIES
            urlOrClass.contains("cartoons") -> FilmType.CARTOON
            urlOrClass.contains("animation") -> FilmType.ANIME
            urlOrClass.contains("show") -> FilmType.TV_SHOW
            else -> FilmType.FILM
        }
    }

    fun parseFilmCard(element: Element): FilmCard? {
        val id = element.attr("data-id")
        val linkElem = element.selectFirst(".b-content__inline_item-link a") ?: return null
        val link = linkElem.attr("href")
        val title = linkElem.text().trim()
        val catClass = element.selectFirst(".cat")?.attr("class") ?: ""
        val type = parseFilmType(catClass.ifEmpty { link })
        val poster = element.selectFirst(".b-content__inline_item-cover img")?.attr("src") ?: ""
        val subtitle = element.selectFirst(".b-content__inline_item-link div")?.text()?.trim() ?: ""
        val info = element.selectFirst(".b-content__inline_item-cover .info")?.text()?.trim() ?: ""
        val isPending = element.selectFirst(".b-content__inline_item-cover")?.hasClass("wait") ?: false

        return FilmCard(
            id = id,
            link = link,
            type = type,
            poster = poster,
            title = title,
            subtitle = subtitle,
            info = info,
            isPendingRelease = isPending
        )
    }

    fun parseFilmCards(html: String): List<FilmCard> {
        val doc = Jsoup.parse(html)
        val items = doc.select(".b-content__inline_item")
        return items.mapNotNull { parseFilmCard(it) }
    }

    fun parseFilmDetails(html: String, link: String): FilmDetails {
        val doc = Jsoup.parse(html)
        val id = doc.selectFirst("#user-favorites-holder")?.attr("data-post_id") ?: ""
        val title = doc.selectFirst(".b-post__title h1")?.text()?.trim() ?: ""
        val origTitle = doc.selectFirst(".b-post__origtitle")?.text()?.trim()
        val poster = doc.selectFirst(".b-sidecover img")?.attr("src") ?: ""
        val largePoster = doc.selectFirst(".b-sidecover a")?.attr("href") ?: poster
        val description = doc.selectFirst(".b-post__description_text")?.text()?.trim() ?: ""

        val ratingNum = doc.selectFirst(".b-post__rating .num")?.text()?.trim() ?: ""
        val ratingVotes = doc.selectFirst(".b-post__rating .votes")?.text()?.replace("(", "")?.replace(")", "")?.trim() ?: ""

        val genres = mutableListOf<String>()
        val countries = mutableListOf<String>()
        val directors = mutableListOf<String>()
        val actors = mutableListOf<String>()
        var releaseDate = ""
        var duration = ""

        doc.select(".b-post__info tr").forEach { row ->
            val key = row.selectFirst("td:first-child")?.text()?.replace(":", "")?.trim() ?: ""
            val valueTd = row.selectFirst("td:last-child") ?: return@forEach

            when {
                key.contains("Жанр") -> {
                    genres.addAll(valueTd.select("a").map { it.text().trim() })
                }
                key.contains("Страна") -> {
                    countries.addAll(valueTd.select("a").map { it.text().trim() })
                }
                key.contains("Режиссер") -> {
                    directors.addAll(valueTd.select(".person-name-item").map { it.text().trim() })
                }
                key.contains("В ролях") -> {
                    actors.addAll(valueTd.select(".person-name-item").map { it.text().trim() })
                }
                key.contains("Дата выхода") -> {
                    releaseDate = valueTd.text().trim()
                }
                key.contains("Время") -> {
                    duration = valueTd.text().trim()
                }
            }
        }

        // Parse voices
        val voices = mutableListOf<FilmVoice>()
        doc.select(".b-translator__item").forEach { voiceElem ->
            val vId = voiceElem.attr("data-translator_id")
            val vTitle = voiceElem.attr("title").ifEmpty { voiceElem.text().trim() }
            val isCamrip = voiceElem.attr("data-camrip").ifEmpty { "0" }
            val isDirector = voiceElem.attr("data-director").ifEmpty { "0" }
            val isAds = voiceElem.attr("data-ad").ifEmpty { "0" }
            val isActive = voiceElem.hasClass("active")
            val isPrem = voiceElem.hasClass("b-prem_translator")
            val img = voiceElem.selectFirst("img")?.attr("src")

            voices.add(
                FilmVoice(
                    id = vId,
                    identifier = "$vId-$isDirector-$isCamrip-$isPrem-$isAds",
                    title = vTitle,
                    img = img,
                    isCamrip = isCamrip,
                    isDirector = isDirector,
                    isAds = isAds,
                    isActive = isActive,
                    isPremium = isPrem,
                    seasons = if (isActive) parseSeasonsFromDoc(doc) else emptyList()
                )
            )
        }

        // If no translator items, check inline script
        if (voices.isEmpty()) {
            val scriptContent = doc.html()
            val parsedSeasons = parseSeasonsFromDoc(doc)

            if (scriptContent.contains("initCDNMoviesEvents")) {
                val movieJson = extractJsonObject(scriptContent, "initCDNMoviesEvents")
                val video = movieJson?.let {
                    val streamStr = it.optString("streams")
                    val subtitleStr = it.optString("subtitle")
                    val subtitleDef = it.optString("subtitle_def")
                    val thumbs = it.optString("thumbnails")
                    FilmVideo(
                        streams = parseStreams(streamStr),
                        subtitles = parseSubtitles(subtitleStr, subtitleDef),
                        storyboardUrl = thumbs
                    )
                }
                voices.add(
                    FilmVoice(
                        id = "",
                        identifier = "movie-default",
                        title = "Оригинал / Дубляж",
                        isActive = true,
                        video = video
                    )
                )
            } else if (scriptContent.contains("initCDNSeriesEvents")) {
                val seriesVoiceId = extractSeriesVoiceId(scriptContent)
                voices.add(
                    FilmVoice(
                        id = seriesVoiceId,
                        identifier = "$seriesVoiceId-series",
                        title = "Основная озвучка",
                        isActive = true,
                        seasons = parsedSeasons
                    )
                )
            }
        }

        // Related items
        val related = doc.select(".b-sidelist .b-content__inline_item").mapNotNull { parseFilmCard(it) }

        return FilmDetails(
            id = id,
            link = link,
            type = parseFilmType(link),
            title = title,
            originalTitle = origTitle,
            poster = poster,
            largePoster = largePoster,
            description = description,
            releaseDate = releaseDate,
            duration = duration,
            rating = ratingNum,
            votes = ratingVotes,
            genres = genres,
            countries = countries,
            directors = directors,
            actors = actors,
            voices = voices,
            related = related,
            isPendingRelease = doc.selectFirst(".b-post__go_status") != null
        )
    }

    fun parseStreams(streamsEncryptedOrPlain: String?): List<FilmStream> {
        if (streamsEncryptedOrPlain.isNullOrBlank()) return emptyList()

        val decoded = StreamDecoder.decrypt(streamsEncryptedOrPlain)
        val result = mutableListOf<FilmStream>()
        val entries = decoded.split(",")

        for (entry in entries) {
            val trimmed = entry.trim()
            if (trimmed.isEmpty()) continue

            val bracketClose = trimmed.indexOf(']')
            if (bracketClose == -1) continue

            val qualityRaw = trimmed.substring(1, bracketClose).trim()
            val urlPart = trimmed.substring(bracketClose + 1).trim()
            val finalUrl = if (urlPart.contains(" or ")) {
                urlPart.split(" or ")[0].trim()
            } else {
                urlPart
            }

            val cleanQuality = qualityRaw.replace(Regex("<[^>]*>"), "").trim()

            if (finalUrl.startsWith("http")) {
                result.add(FilmStream(quality = cleanQuality, url = finalUrl))
            }
        }

        return result
    }

    fun parseSeasonsFromDoc(doc: Document): List<Season> {
        val seasons = mutableListOf<Season>()
        val seasonTabs = doc.select(".b-simple_season__item")

        if (seasonTabs.isNotEmpty()) {
            seasonTabs.forEach { tab ->
                val seasonId = tab.attr("data-tab_id")
                val seasonName = tab.text().trim()
                val episodes = mutableListOf<Episode>()

                doc.select("#simple-episodes-list-$seasonId .b-simple_episode__item").forEach { ep ->
                    episodes.add(
                        Episode(
                            episodeId = ep.attr("data-episode_id"),
                            name = ep.text().trim()
                        )
                    )
                }

                seasons.add(
                    Season(
                        seasonId = seasonId,
                        name = seasonName,
                        episodes = episodes
                    )
                )
            }
        } else {
            // Some series only have episode lists without seasons tabs
            val episodeItems = doc.select(".b-simple_episode__item")
            if (episodeItems.isNotEmpty()) {
                val episodes = episodeItems.map { ep ->
                    Episode(
                        episodeId = ep.attr("data-episode_id"),
                        name = ep.text().trim()
                    )
                }
                seasons.add(
                    Season(
                        seasonId = "1",
                        name = "Сезон 1",
                        episodes = episodes,
                        isOnlyEpisodes = true
                    )
                )
            }
        }

        return seasons
    }

    fun parseSeasonsFromHtml(html: String): List<Season> {
        val doc = Jsoup.parse("<div>$html</div>")
        return parseSeasonsFromDoc(doc)
    }

    fun parseSubtitles(subtitleStr: String?, defaultLang: String? = null): List<Subtitle> {
        if (subtitleStr.isNullOrBlank()) return emptyList()
        val list = mutableListOf<Subtitle>()
        subtitleStr.split(",").forEach { entry ->
            val bracketClose = entry.indexOf(']')
            if (bracketClose != -1) {
                val lang = entry.substring(1, bracketClose).trim()
                val url = entry.substring(bracketClose + 1).trim()
                list.add(
                    Subtitle(
                        name = lang,
                        url = url,
                        isDefault = defaultLang != null && lang.contains(defaultLang, ignoreCase = true)
                    )
                )
            }
        }
        return list
    }

    private fun extractJsonObject(content: String, marker: String): JSONObject? {
        val startIdx = content.indexOf(marker)
        if (startIdx == -1) return null
        val braceOpen = content.indexOf("{\"id\"", startIdx)
        if (braceOpen == -1) return null
        val braceClose = content.indexOf("});", braceOpen)
        if (braceClose == -1) return null
        val jsonStr = content.substring(braceOpen, braceClose + 1)
        return try {
            JSONObject(jsonStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractSeriesVoiceId(content: String): String {
        val marker = "initCDNSeriesEvents"
        val start = content.indexOf(marker)
        if (start == -1) return ""
        var end = content.indexOf("{\"id\"", start)
        if (end == -1) end = content.indexOf("{\"url\"", start)
        if (end == -1) return ""
        val sub = content.substring(start, end)
        val parts = sub.split(",")
        return if (parts.size > 1) parts[1].replace(" ", "").trim() else ""
    }
}
