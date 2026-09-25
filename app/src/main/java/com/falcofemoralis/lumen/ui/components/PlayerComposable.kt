package com.falcofemoralis.lumen.ui.components

import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.falcofemoralis.lumen.data.model.FilmStream
import com.falcofemoralis.lumen.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerComposable(
    title: String,
    subtitle: String? = null,
    streams: List<FilmStream>,
    initialStreamIndex: Int = 0,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedStreamIndex by remember { mutableIntStateOf(initialStreamIndex.coerceIn(0, (streams.size - 1).coerceAtLeast(0))) }
    val currentStream = streams.getOrNull(selectedStreamIndex)

    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isQualityDialogOpen by remember { mutableStateOf(false) }
    var controlsTimerTrigger by remember { mutableIntStateOf(0) }

    val playPauseFocusRequester = remember { FocusRequester() }

    // Initialize ExoPlayer
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Load media when stream changes
    LaunchedEffect(currentStream?.url) {
        currentStream?.url?.let { url ->
            val mediaItem = MediaItem.fromUri(url)
            val currentPos = exoPlayer.currentPosition
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (currentPos > 0) {
                exoPlayer.seekTo(currentPos)
            }
            exoPlayer.play()
        }
    }

    // Attach listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                durationMs = exoPlayer.duration.coerceAtLeast(0L)
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Position ticker
    LaunchedEffect(isPlaying) {
        while (true) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.coerceAtLeast(0L)
            delay(250)
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(isControlsVisible, controlsTimerTrigger, isPlaying) {
        if (isControlsVisible && isPlaying && !isQualityDialogOpen) {
            delay(4000)
            isControlsVisible = false
        }
    }

    fun pokeControls() {
        isControlsVisible = true
        controlsTimerTrigger++
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("player_root")
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (!isControlsVisible) {
                                pokeControls()
                                true
                            } else {
                                false
                            }
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            pokeControls()
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            pokeControls()
                            exoPlayer.play()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            pokeControls()
                            exoPlayer.pause()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            pokeControls()
                            val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            pokeControls()
                            val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(durationMs)
                            exoPlayer.seekTo(newPos)
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                            pokeControls()
                            false
                        }
                        KeyEvent.KEYCODE_BACK -> {
                            if (isControlsVisible) {
                                isControlsVisible = false
                                true
                            } else {
                                onBack()
                                true
                            }
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                pokeControls()
            }
    ) {
        // Player Surface View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Controls
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
            ) {
                // Top Action Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xCC000000), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlayerTvButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            onClick = onBack
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!subtitle.isNullOrBlank()) {
                                Text(
                                    text = subtitle,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Quality Selector Button
                    if (streams.isNotEmpty()) {
                        PlayerTvButton(
                            text = currentStream?.quality ?: "Качество",
                            icon = Icons.Default.HighQuality,
                            contentDescription = "Качество видео",
                            onClick = { isQualityDialogOpen = true }
                        )
                    }
                }

                // Center Play/Pause & Rewind Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp)
                ) {
                    PlayerTvButton(
                        icon = Icons.Default.Replay10,
                        contentDescription = "Назад 10 секунд",
                        onClick = {
                            pokeControls()
                            exoPlayer.seekTo((exoPlayer.currentPosition - 10000L).coerceAtLeast(0L))
                        },
                        size = 56.dp
                    )

                    Spacer(modifier = Modifier.width(36.dp))

                    // Big Play/Pause Focus Button
                    PlayerTvButton(
                        icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Пауза" else "Воспроизведение",
                        isPrimary = true,
                        size = 76.dp,
                        focusRequester = playPauseFocusRequester,
                        onClick = {
                            pokeControls()
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        }
                    )

                    Spacer(modifier = Modifier.width(36.dp))

                    PlayerTvButton(
                        icon = Icons.Default.Forward10,
                        contentDescription = "Вперед 10 секунд",
                        onClick = {
                            pokeControls()
                            exoPlayer.seekTo((exoPlayer.currentPosition + 10000L).coerceAtMost(durationMs))
                        },
                        size = 56.dp
                    )
                }

                // Bottom Timeline & Progress Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xDD000000))
                            )
                        )
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = formatTime(durationMs),
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress Slider
                    val sliderValue = if (durationMs > 0) {
                        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Slider(
                        value = sliderValue,
                        onValueChange = { fraction ->
                            pokeControls()
                            val targetPos = (fraction * durationMs).toLong()
                            exoPlayer.seekTo(targetPos)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = LumenRed,
                            activeTrackColor = LumenRed,
                            inactiveTrackColor = Color(0x66FFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_progress_bar")
                    )
                }
            }
        }

        // Quality Selection Dialog
        if (isQualityDialogOpen) {
            AlertDialog(
                onDismissRequest = { isQualityDialogOpen = false },
                containerColor = DarkSurface,
                title = {
                    Text("Выберите качество", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        streams.forEachIndexed { index, stream ->
                            val isSelected = index == selectedStreamIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LumenRedDark else Color.Transparent)
                                    .clickable {
                                        selectedStreamIndex = index
                                        isQualityDialogOpen = false
                                        pokeControls()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stream.quality,
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Выбрано",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { isQualityDialogOpen = false }) {
                        Text("Закрыть", color = LumenRed)
                    }
                }
            )
        }
    }
}

@Composable
fun PlayerTvButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    text: String? = null,
    contentDescription: String? = null,
    isPrimary: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    focusRequester: FocusRequester? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(if (text == null) Modifier.size(size) else Modifier.height(size))
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .clip(CircleShape)
            .background(
                when {
                    isFocused -> LumenRed
                    isPrimary -> Color(0xEE2A2A35)
                    else -> Color(0x66181820)
                }
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FocusGold else Color(0x44FFFFFF),
                shape = CircleShape
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(if (text != null) Modifier.padding(horizontal = 16.dp) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(if (isPrimary) 38.dp else 24.dp)
                )
            }
            if (text != null) {
                if (icon != null) Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
