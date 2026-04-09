package com.morgans.dashboard.ui.screens.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morgans.dashboard.util.formatBytes
import com.morgans.dashboard.util.formatDuration
import com.morgans.dashboard.util.formatSpeed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(viewModel: DownloadsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf("movie") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Downloads", style = MaterialTheme.typography.headlineLarge)
        }

        // Search bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search torrents...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.search(searchQuery, searchType)
                            }) {
                                Icon(Icons.Filled.Search, "Search")
                            }
                        }
                    },
                )
                FilterChip(
                    selected = searchType == "movie",
                    onClick = { searchType = "movie" },
                    label = { Text("Movie") },
                )
                FilterChip(
                    selected = searchType == "series",
                    onClick = { searchType = "series" },
                    label = { Text("Series") },
                )
            }
        }

        // Search results
        if (state.searchResults.isNotEmpty()) {
            item {
                Text("Search Results", style = MaterialTheme.typography.titleMedium)
            }
            items(state.searchResults) { result ->
                Card {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(result.name, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("${result.size} | S:${result.seeders} L:${result.leechers}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            IconButton(
                                onClick = { viewModel.addTorrent(result.magnet) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(Icons.Filled.Add, "Download", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Active torrents
        if (state.torrents.isNotEmpty()) {
            item {
                Text("Active Downloads", style = MaterialTheme.typography.titleMedium)
            }
            items(state.torrents, key = { it.hash }) { torrent ->
                TorrentCard(
                    torrent = torrent,
                    onPause = { viewModel.pauseTorrent(torrent.hash) },
                    onResume = { viewModel.resumeTorrent(torrent.hash) },
                    onDelete = { viewModel.deleteTorrent(torrent.hash) },
                )
            }
        } else if (!state.loading) {
            item {
                Text(
                    "No active downloads",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // History toggle
        item {
            TextButton(onClick = { viewModel.toggleHistory() }) {
                Icon(
                    if (state.showHistory) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text("Completed History")
            }
        }

        if (state.showHistory) {
            items(state.history) { item ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.name, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${formatBytes(item.size)} | ${item.completedAt}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TorrentCard(
    torrent: com.morgans.dashboard.data.model.Torrent,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
) {
    val isPaused = torrent.state.lowercase().contains("paused")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                torrent.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { torrent.progress.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${"%.1f".format(torrent.progress * 100)}% | ${formatSpeed(torrent.speed)} | ${torrent.peers} peers",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row {
                    IconButton(onClick = { if (isPaused) onResume() else onPause() }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.Delete,
                            "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
