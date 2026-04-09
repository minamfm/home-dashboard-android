package com.morgans.dashboard.ui.screens.photos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.morgans.dashboard.BuildConfig
import com.morgans.dashboard.data.model.PhotoItem
import com.morgans.dashboard.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotosState(
    val photos: List<PhotoItem> = emptyList(),
    val configured: Boolean = true,
    val loading: Boolean = true,
    val selectedPhoto: PhotoItem? = null,
)

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(PhotosState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val response = repository.getPhotos()
                _state.update {
                    it.copy(
                        photos = response.items,
                        configured = response.configured,
                        loading = false,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    fun selectPhoto(photo: PhotoItem?) {
        _state.update { it.copy(selectedPhoto = photo) }
    }
}

@Composable
fun PhotosScreen(viewModel: PhotosViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Photos", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))

        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (!state.configured) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Google Photos not configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(state.photos) { photo ->
                    AsyncImage(
                        model = "${BuildConfig.BASE_URL}${photo.url}",
                        contentDescription = photo.filename,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { viewModel.selectPhoto(photo) },
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }

    // Lightbox
    state.selectedPhoto?.let { photo ->
        Dialog(
            onDismissRequest = { viewModel.selectPhoto(null) },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(Modifier.fillMaxSize()) {
                AsyncImage(
                    model = "${BuildConfig.BASE_URL}${photo.fullUrl}",
                    contentDescription = photo.filename,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                IconButton(
                    onClick = { viewModel.selectPhoto(null) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                ) {
                    Icon(Icons.Filled.Close, "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
