package com.morgans.dashboard.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SmartHomeResponse(
    val rooms: List<Room> = emptyList(),
    val deviceCount: DeviceCount? = null,
)

@Serializable
data class Room(
    val name: String,
    val devices: List<Device> = emptyList(),
)

@Serializable
data class Device(
    val entity_id: String,
    val name: String,
    val state: String,
    val domain: String = "",
    val category: String = "",
    val attributes: DeviceAttributes = DeviceAttributes(),
)

@Serializable
data class DeviceAttributes(
    val brightness: Int? = null,
    val color_temp: Int? = null,
    val min_mireds: Int? = null,
    val max_mireds: Int? = null,
    val rgb_color: List<Int>? = null,
    val hs_color: List<Double>? = null,
    val current_position: Int? = null,
    val temperature: Double? = null,
    val current_temperature: Double? = null,
    val humidity: Double? = null,
    val unit_of_measurement: String? = null,
    val device_class: String? = null,
    val friendly_name: String? = null,
)

@Serializable
data class DeviceCount(
    val total: Int = 0,
    val active: Int = 0,
    val unavailable: Int = 0,
)

@Serializable
data class PowerHealthResponse(
    val voltage: Double? = null,
    val status: String = "unknown",
    @SerialName("sensor_count") val sensorCount: Int = 0,
    val devices: List<PowerDevice> = emptyList(),
    val stats24h: PowerStats? = null,
)

@Serializable
data class PowerDevice(
    val entity_id: String,
    val name: String,
    val state: String,
    val voltage: Double? = null,
)

@Serializable
data class PowerStats(
    val min: Double? = null,
    val max: Double? = null,
    val avg: Double? = null,
)

@Serializable
data class ServiceCallRequest(
    val entity_id: String,
    val domain: String,
    val service: String,
    val data: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
)

@Serializable
data class HiddenRoomsResponse(
    val rooms: List<String> = emptyList(),
)
