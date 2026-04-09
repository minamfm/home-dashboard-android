package com.morgans.dashboard.ui.screens.media

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.morgans.dashboard.BuildConfig

@Composable
fun MediaScreen(viewModel: MediaViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.view != MediaView.LIBRARIES) {
        viewModel.goBack()
    }

    when (state.view) {
        MediaView.LIBRARIES -> LibrariesView(state, viewModel)
        MediaView.ITEMS -> LibraryItemsView(state, viewModel)
        MediaView.PLAYER -> PlayerView(state, viewModel)
    }
}

@Composable
private fun LibrariesView(state: MediaState, viewModel: MediaViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Media", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))

        // Continue Watching
        if (state.continueWatching.isNotEmpty()) {
            Text("Continue Watching", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.continueWatching) { item ->
                    Card(
                        modifier = Modifier.width(150.dp).clickable { viewModel.openItem(item) },
                    ) {
                        Column {
                            if (item.thumb != null) {
                                AsyncImage(
                                    model = "${BuildConfig.BASE_URL}${item.thumb}",
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(8.dp),
                            )
                            if (item.viewOffset != null && item.duration != null && item.duration > 0) {
                                LinearProgressIndicator(
                                    progress = { (item.viewOffset.toFloat() / item.duration.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        Text("Libraries", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (state.loading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.libraries) { library ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.openLibrary(library) },
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                when (library.type) {
                                    "movie" -> Icons.Filled.Movie
                                    "show" -> Icons.Filled.Tv
                                    "artist" -> Icons.Filled.MusicNote
                                    else -> Icons.Filled.Folder
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(library.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${library.count} items",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryItemsView(state: MediaState, viewModel: MediaViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Text(
                state.currentLibrary?.title ?: "Library",
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        Spacer(Modifier.height(12.dp))

        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.items) { item ->
                    Card(
                        modifier = Modifier.clickable { viewModel.openItem(item) },
                    ) {
                        Column {
                            if (item.thumb != null) {
                                AsyncImage(
                                    model = "${BuildConfig.BASE_URL}${item.thumb}",
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxWidth().height(200.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    item.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                item.year?.let {
                                    Text(
                                        it.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun PlayerView(state: MediaState, viewModel: MediaViewModel) {
    val detail = state.currentDetail ?: return
    val streamUrl = detail.streamUrl ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { viewModel.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Text(detail.title, style = MaterialTheme.typography.titleMedium)
        }

        // ExoPlayer video
        val fullUrl = "${BuildConfig.BASE_URL}$streamUrl"
        var player by remember { mutableStateOf<ExoPlayer?>(null) }

        AndroidView(
            factory = { context ->
                val exoPlayer = ExoPlayer.Builder(context).build().apply {
                    setMediaItem(ExoMediaItem.fromUri(fullUrl))
                    prepare()
                    playWhenReady = true
                }
                player = exoPlayer
                PlayerView(context).apply {
                    this.player = exoPlayer
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            onRelease = {
                val currentPlayer = player
                if (currentPlayer != null) {
                    val pos = currentPlayer.currentPosition
                    state.currentItem?.let { item ->
                        viewModel.saveProgress(item.ratingKey, pos)
                    }
                    currentPlayer.release()
                }
            },
        )

        // Info
        Column(modifier = Modifier.padding(16.dp)) {
            detail.year?.let {
                Text("$it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            detail.summary?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
