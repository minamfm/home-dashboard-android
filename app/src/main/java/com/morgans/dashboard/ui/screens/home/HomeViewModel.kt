package com.morgans.dashboard.ui.screens.home

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
import java.time.LocalDate
import javax.inject.Inject

data class HomeState(
    val deviceCount: DeviceCount? = null,
    val powerHealth: PowerHealthResponse? = null,
    val libraryCount: Int = 0,
    val activeTorrents: Int = 0,
    val monthlyExpenses: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val recentlyAdded: List<MediaItem> = emptyList(),
    val loading: Boolean = true,
    val showPowerModal: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadAll()
        startPolling()
    }

    private fun loadAll() {
        viewModelScope.launch {
            try {
                val smartHome = repository.getSmartHome()
                _state.update { it.copy(deviceCount = smartHome.deviceCount) }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                val libs = repository.getLibraries()
                _state.update { it.copy(libraryCount = libs.libraries.size) }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                val torrents = repository.getTorrents()
                _state.update { it.copy(activeTorrents = torrents.torrents.size) }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                val now = LocalDate.now()
                val expenses = repository.getExpenses(now.monthValue, now.year)
                val income = repository.getIncome(now.monthValue, now.year)
                _state.update {
                    it.copy(
                        monthlyExpenses = expenses.totals?.total ?: 0.0,
                        monthlyIncome = income.total,
                    )
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                val recent = repository.getRecentlyAdded()
                _state.update { it.copy(recentlyAdded = recent.items) }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            loadPowerHealth()
            _state.update { it.copy(loading = false) }
        }
    }

    private suspend fun loadPowerHealth() {
        try {
            val health = repository.getPowerHealth()
            _state.update { it.copy(powerHealth = health) }
        } catch (_: Exception) {}
    }

    private fun startPolling() {
        // Power health every 5s
        viewModelScope.launch {
            while (isActive) {
                delay(5000)
                loadPowerHealth()
            }
        }
        // General data every 30s
        viewModelScope.launch {
            while (isActive) {
                delay(30000)
                loadAll()
            }
        }
    }

    fun togglePowerModal() {
        _state.update { it.copy(showPowerModal = !it.showPowerModal) }
    }
}
