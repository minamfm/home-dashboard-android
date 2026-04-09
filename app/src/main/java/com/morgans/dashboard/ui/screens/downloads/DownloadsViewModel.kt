package com.morgans.dashboard.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morgans.dashboard.data.model.*
import com.morgans.dashboard.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsState(
    val torrents: List<Torrent> = emptyList(),
    val history: List<TorrentHistoryItem> = emptyList(),
    val searchResults: List<TorrentSearchResult> = emptyList(),
    val searchQuery: String = "",
    val searchType: String = "movie",
    val searching: Boolean = false,
    val loading: Boolean = true,
    val showHistory: Boolean = false,
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DownloadsState())
    val state = _state.asStateFlow()

    init {
        loadTorrents()
        startPolling()
    }

    fun loadTorrents() {
        viewModelScope.launch {
            try {
                val response = repository.getTorrents()
                _state.update { it.copy(torrents = response.torrents, loading = false) }
            } catch (_: Exception) {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                val response = repository.getTorrentHistory()
                _state.update { it.copy(history = response.history) }
            } catch (_: Exception) {}
        }
    }

    fun search(query: String, type: String) {
        _state.update { it.copy(searchQuery = query, searchType = type, searching = true) }
        viewModelScope.launch {
            try {
                val results = repository.searchTorrents(query, type)
                _state.update { it.copy(searchResults = results.results, searching = false) }
            } catch (_: Exception) {
                _state.update { it.copy(searching = false) }
            }
        }
    }

    fun addTorrent(magnet: String) {
        viewModelScope.launch {
            try {
                repository.addTorrent(magnet)
                delay(500)
                loadTorrents()
            } catch (_: Exception) {}
        }
    }

    fun deleteTorrent(hash: String) {
        viewModelScope.launch {
            try {
                repository.deleteTorrent(hash)
                loadTorrents()
            } catch (_: Exception) {}
        }
    }

    fun pauseTorrent(hash: String) {
        viewModelScope.launch {
            try {
                repository.pauseTorrent(hash)
                delay(300)
                loadTorrents()
            } catch (_: Exception) {}
        }
    }

    fun resumeTorrent(hash: String) {
        viewModelScope.launch {
            try {
                repository.resumeTorrent(hash)
                delay(300)
                loadTorrents()
            } catch (_: Exception) {}
        }
    }

    fun toggleHistory() {
        val show = !_state.value.showHistory
        _state.update { it.copy(showHistory = show) }
        if (show) loadHistory()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(2000)
                loadTorrents()
            }
        }
    }
}
