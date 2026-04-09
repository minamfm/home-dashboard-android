package com.morgans.dashboard.ui.screens.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morgans.dashboard.data.model.*
import com.morgans.dashboard.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MediaView { LIBRARIES, ITEMS, PLAYER }

data class MediaState(
    val view: MediaView = MediaView.LIBRARIES,
    val libraries: List<Library> = emptyList(),
    val items: List<MediaItem> = emptyList(),
    val continueWatching: List<MediaItem> = emptyList(),
    val currentLibrary: Library? = null,
    val currentDetail: MediaDetailResponse? = null,
    val currentItem: MediaItem? = null,
    val loading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MediaState())
    val state = _state.asStateFlow()

    init { loadLibraries() }

    fun loadLibraries() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val libs = repository.getLibraries()
                val cw = try { repository.getContinueWatching() } catch (_: Exception) { ContinueWatchingResponse() }
                _state.update {
                    it.copy(
                        libraries = libs.libraries,
                        continueWatching = cw.items,
                        view = MediaView.LIBRARIES,
                        loading = false,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun openLibrary(library: Library) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, currentLibrary = library) }
            try {
                val items = repository.getLibraryItems(library.key)
                _state.update {
                    it.copy(items = items.items, view = MediaView.ITEMS, loading = false)
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun openItem(item: MediaItem) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, currentItem = item) }
            try {
                val detail = repository.getMediaDetail(item.ratingKey)
                _state.update {
                    it.copy(currentDetail = detail, view = MediaView.PLAYER, loading = false)
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun goBack() {
        _state.update {
            when (it.view) {
                MediaView.PLAYER -> it.copy(view = MediaView.ITEMS, currentDetail = null, currentItem = null)
                MediaView.ITEMS -> it.copy(view = MediaView.LIBRARIES, items = emptyList(), currentLibrary = null)
                MediaView.LIBRARIES -> it
            }
        }
    }

    fun saveProgress(ratingKey: String, offset: Long) {
        viewModelScope.launch {
            try { repository.saveProgress(ratingKey, offset) } catch (_: Exception) {}
        }
    }
}
