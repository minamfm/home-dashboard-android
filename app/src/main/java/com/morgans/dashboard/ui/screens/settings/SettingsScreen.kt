package com.morgans.dashboard.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.morgans.dashboard.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import javax.inject.Inject

data class SettingsState(
    val settings: Map<String, String> = emptyMap(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val saved: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val json = repository.getSettings()
                val flat = mutableMapOf<String, String>()
                json.forEach { (key, value) ->
                    flat[key] = when (value) {
                        is JsonPrimitive -> value.content
                        else -> value.toString()
                    }
                }
                _state.update { it.copy(settings = flat, loading = false) }
            } catch (_: Exception) {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    fun updateSetting(key: String, value: String) {
        _state.update { it.copy(settings = it.settings + (key to value), saved = false) }
    }

    fun save() {
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            try {
                val jsonObj = buildJsonObject {
                    _state.value.settings.forEach { (k, v) -> put(k, v) }
                }
                repository.saveSettings(jsonObj)
                _state.update { it.copy(saving = false, saved = true) }
            } catch (_: Exception) {
                _state.update { it.copy(saving = false) }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)

        if (state.loading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Group settings by prefix
            val sections = state.settings.entries.groupBy { entry ->
                entry.key.substringBefore("_").replaceFirstChar { it.uppercase() }
            }

            sections.forEach { (section, entries) ->
                Text(section, style = MaterialTheme.typography.titleMedium)
                entries.forEach { (key, value) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { viewModel.updateSetting(key, it) },
                        label = { Text(key.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { viewModel.load() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = { viewModel.save() },
                    modifier = Modifier.weight(1f),
                    enabled = !state.saving,
                ) {
                    if (state.saving) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (state.saved) "Saved!" else "Save")
                    }
                }
            }
        }
    }
}
