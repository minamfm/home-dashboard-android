package com.morgans.dashboard.ui.screens.logs

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.morgans.dashboard.data.model.ActivityLog
import com.morgans.dashboard.data.model.ActivityStatsResponse
import com.morgans.dashboard.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogsState(
    val logs: List<ActivityLog> = emptyList(),
    val stats: ActivityStatsResponse? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val loading: Boolean = true,
)

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LogsState())
    val state = _state.asStateFlow()

    init {
        load()
        startPolling()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val logs = repository.getActivityLogs(page = 1, category = null, user = null)
                val stats = repository.getActivityStats()
                _state.update {
                    it.copy(
                        logs = logs.logs,
                        stats = stats,
                        page = 1,
                        hasMore = logs.logs.size >= 50,
                        loading = false,
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    fun loadMore() {
        val nextPage = _state.value.page + 1
        viewModelScope.launch {
            try {
                val logs = repository.getActivityLogs(page = nextPage, category = null, user = null)
                _state.update {
                    it.copy(
                        logs = it.logs + logs.logs,
                        page = nextPage,
                        hasMore = logs.logs.size >= 50,
                    )
                }
            } catch (_: Exception) {}
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(15000)
                try {
                    val stats = repository.getActivityStats()
                    _state.update { it.copy(stats = stats) }
                } catch (_: Exception) {}
            }
        }
    }
}

@Composable
fun LogsScreen(viewModel: LogsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("Activity", style = MaterialTheme.typography.headlineLarge)
        }

        // Stats row
        state.stats?.let { stats ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatChip(Modifier.weight(1f), "${stats.actionsToday}", "Today")
                    StatChip(Modifier.weight(1f), stats.mostActiveUser ?: "-", "Top User")
                    StatChip(Modifier.weight(1f), "${stats.activeUsers}", "Active")
                    StatChip(Modifier.weight(1f), stats.topCategory ?: "-", "Top Cat")
                }
            }
        }

        if (state.loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(state.logs, key = { it.id }) { log ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.action, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${log.username} | ${log.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            log.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (state.hasMore) {
                item {
                    TextButton(
                        onClick = { viewModel.loadMore() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Load More")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(modifier: Modifier = Modifier, value: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
