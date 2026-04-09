package com.morgans.dashboard.ui.screens.smarthome

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morgans.dashboard.data.model.Device
import com.morgans.dashboard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartHomeScreen(
    viewModel: SmartHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Smart Home", style = MaterialTheme.typography.headlineLarge)
            IconButton(onClick = { viewModel.loadDevices() }) {
                Icon(Icons.Filled.Refresh, "Refresh")
            }
        }

        // Room tabs
        if (state.rooms.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.rooms) { room ->
                    FilterChip(
                        selected = state.selectedRoom == room.name,
                        onClick = { viewModel.selectRoom(room.name) },
                        label = { Text(room.name) },
                    )
                }
            }
        }

        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            val currentRoom = state.rooms.find { it.name == state.selectedRoom }
            val devices = currentRoom?.devices ?: emptyList()
            val grouped = devices.groupBy { SmartHomeViewModel.categoryForDevice(it) }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                grouped.forEach { (category, devicesInCategory) ->
                    item {
                        Text(
                            category,
                            style = MaterialTheme.typography.titleMedium,
                            color = categoryColor(category),
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(devicesInCategory, key = { it.entity_id }) { device ->
                        DeviceCard(
                            device = device,
                            onToggle = { viewModel.toggleDevice(device) },
                            onBrightnessChange = { viewModel.setDeviceBrightness(device, it) },
                            onPositionChange = { viewModel.setCoverPosition(device, it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: Device,
    onToggle: () -> Unit,
    onBrightnessChange: (Int) -> Unit,
    onPositionChange: (Int) -> Unit,
) {
    val isActive = SmartHomeViewModel.isDeviceActive(device)
    val isSensor = device.domain == "sensor" || device.domain == "binary_sensor"

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive && !isSensor)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        device.attributes.friendly_name ?: device.name,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        if (isSensor) "${device.state}${device.attributes.unit_of_measurement ?: ""}"
                        else device.state.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (!isSensor) {
                    Switch(
                        checked = isActive,
                        onCheckedChange = { onToggle() },
                    )
                }
            }

            // Brightness slider for lights
            if (device.domain == "light" && isActive && device.attributes.brightness != null) {
                var brightness by remember(device.attributes.brightness) {
                    mutableFloatStateOf(device.attributes.brightness!!.toFloat())
                }
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    onValueChangeFinished = { onBrightnessChange(brightness.toInt()) },
                    valueRange = 0f..255f,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            // Position slider for covers
            if (device.domain == "cover" && device.attributes.current_position != null) {
                var position by remember(device.attributes.current_position) {
                    mutableFloatStateOf(device.attributes.current_position!!.toFloat())
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Icon(Icons.Filled.VerticalAlignBottom, null, Modifier.size(16.dp))
                    Slider(
                        value = position,
                        onValueChange = { position = it },
                        onValueChangeFinished = { onPositionChange(position.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f),
                    )
                    Text("${position.toInt()}%", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun categoryColor(category: String): Color = when (category) {
    "Lights" -> CategoryLights
    "Switches" -> CategorySwitches
    "Sensors" -> CategorySensors
    "Climate" -> CategoryClimate
    "Fans" -> CategoryFans
    "Locks" -> CategoryLocks
    "Media" -> CategoryMedia
    "Curtains" -> CategoryCurtains
    else -> Color.Gray
}
