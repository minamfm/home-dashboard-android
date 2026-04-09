package com.morgans.dashboard.ui.screens.smarthome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morgans.dashboard.data.model.*
import com.morgans.dashboard.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

data class SmartHomeState(
    val rooms: List<Room> = emptyList(),
    val selectedRoom: String? = null,
    val loading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class SmartHomeViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SmartHomeState())
    val state = _state.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val response = repository.getSmartHome()
                _state.update {
                    it.copy(
                        rooms = response.rooms,
                        selectedRoom = it.selectedRoom ?: response.rooms.firstOrNull()?.name,
                        loading = false,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun selectRoom(name: String) {
        _state.update { it.copy(selectedRoom = name) }
    }

    fun toggleDevice(device: Device) {
        val isActive = isDeviceActive(device)
        val (domain, service) = when (device.domain) {
            "light" -> "light" to if (isActive) "turn_off" else "turn_on"
            "switch" -> "switch" to if (isActive) "turn_off" else "turn_on"
            "fan" -> "fan" to if (isActive) "turn_off" else "turn_on"
            "lock" -> "lock" to if (isActive) "unlock" else "lock"
            "cover" -> "cover" to if (isActive) "close_cover" else "open_cover"
            "media_player" -> "media_player" to if (isActive) "turn_off" else "turn_on"
            else -> return
        }

        viewModelScope.launch {
            try {
                repository.callService(
                    ServiceCallRequest(
                        entity_id = device.entity_id,
                        domain = domain,
                        service = service,
                    )
                )
                // Refresh after a short delay
                kotlinx.coroutines.delay(500)
                loadDevices()
            } catch (_: Exception) {}
        }
    }

    fun setDeviceBrightness(device: Device, brightness: Int) {
        viewModelScope.launch {
            try {
                repository.callService(
                    ServiceCallRequest(
                        entity_id = device.entity_id,
                        domain = "light",
                        service = "turn_on",
                        data = mapOf("brightness" to JsonPrimitive(brightness)),
                    )
                )
            } catch (_: Exception) {}
        }
    }

    fun setCoverPosition(device: Device, position: Int) {
        viewModelScope.launch {
            try {
                repository.callService(
                    ServiceCallRequest(
                        entity_id = device.entity_id,
                        domain = "cover",
                        service = "set_cover_position",
                        data = mapOf("position" to JsonPrimitive(position)),
                    )
                )
            } catch (_: Exception) {}
        }
    }

    companion object {
        fun isDeviceActive(device: Device): Boolean {
            val state = device.state.lowercase()
            return when (device.domain) {
                "cover" -> {
                    val pos = device.attributes.current_position
                    if (pos != null) pos > 0
                    else state == "open" || state == "on"
                }
                else -> state == "on" || state == "playing" || state == "locked"
            }
        }

        fun categoryForDevice(device: Device): String {
            return when (device.domain) {
                "light" -> "Lights"
                "switch" -> "Switches"
                "sensor", "binary_sensor" -> "Sensors"
                "climate" -> "Climate"
                "fan" -> "Fans"
                "lock" -> "Locks"
                "media_player" -> "Media"
                "cover" -> "Curtains"
                else -> "Other"
            }
        }
    }
}
