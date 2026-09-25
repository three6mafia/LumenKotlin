# Lumen — Android & Android TV Client

Lumen is a modern native Android application written in **Kotlin** and **Jetpack Compose** with dedicated **Android TV** support.

## Architecture & Tech Stack

- **Target Platforms**: Android (Mobile / Tablets) & Android TV (Leanback launcher)
- **UI Framework**: Jetpack Compose, Material 3, TvLazyColumn, TvLazyRow
- **Video Playback**: AndroidX Media3 ExoPlayer with custom D-pad remote controls
- **Networking**: Retrofit 2, OkHttp 4, Coroutines, Flow, StateFlow
- **HTML Parsing & Stream Decryption**: Jsoup + native Kotlin stream cipher decryption algorithm
- **Image Loading**: Coil Compose

## Features

- ⭐️ **Full Android TV & D-pad Support**: Full navigation using TV remotes, focused scale animations and focus rings.
- ⭐️ **TvLazyColumn & TvLazyRow Shelves**: Dynamic catalog shelves ("Горячие Новинки", "Новинки", "Сейчас смотрят", "Популярные", "Фильмы", "Сериалы", "Аниме", "В ожидании").
- ⭐️ **Custom Media3 ExoPlayer**: Play/Pause, 10s Rewind/Forward, interactive progress slider, quality selector, and auto-hiding controls.
- ⭐️ **Film & Series Details**: Voice acting selector, seasons and episodes, metadata, and related films.
- ⭐️ **Instant Search & Bookmarks**: Fast search suggestions and offline favorites storage.

## License

[MIT](./LICENSE)
