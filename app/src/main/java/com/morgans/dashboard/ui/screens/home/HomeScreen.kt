package com.morgans.dashboard.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.morgans.dashboard.BuildConfig
import com.morgans.dashboard.ui.theme.*
import com.morgans.dashboard.util.formatCurrency

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Dashboard",
            style = MaterialTheme.typography.headlineLarge,
        )

        // Dashboard tiles grid — 2 columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardTile(
                modifier = Modifier.weight(1f),
                title = "Smart Home",
                icon = Icons.Filled.Home,
                iconTint = StatusOnline,
                value = "${state.deviceCount?.active ?: 0}/${state.deviceCount?.total ?: 0}",
                subtitle = "${state.deviceCount?.unavailable ?: 0} unavailable",
                onClick = { onNavigate("smarthome") },
            )
            DashboardTile(
                modifier = Modifier.weight(1f),
                title = "Plex Media",
                icon = Icons.Filled.Movie,
                iconTint = CategoryMedia,
                value = "${state.libraryCount}",
                subtitle = "libraries",
                onClick = { onNavigate("media") },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardTile(
                modifier = Modifier.weight(1f),
                title = "Downloads",
                icon = Icons.Filled.Download,
                iconTint = CategorySwitches,
                value = "${state.activeTorrents}",
                subtitle = "active",
                onClick = { onNavigate("downloads") },
            )
            DashboardTile(
                modifier = Modifier.weight(1f),
                title = "Finances",
                icon = Icons.Filled.AccountBalanceWallet,
                iconTint = CategoryLights,
                value = formatCurrency(state.monthlyExpenses),
                subtitle = "this month",
                onClick = { onNavigate("expenses") },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val voltage = state.powerHealth?.voltage
            val powerStatus = state.powerHealth?.status ?: "unknown"
            DashboardTile(
                modifier = Modifier.weight(1f),
                title = "Power Health",
                icon = Icons.Outlined.Bolt,
                iconTint = if (powerStatus == "normal") StatusOnline else StatusWarning,
                value = if (voltage != null) "%.0fV".format(voltage) else "--",
                subtitle = powerStatus,
                onClick = { viewModel.togglePowerModal() },
            )
            DashboardTile(
                modifier = Modifier.weight(1f),
                title = "Net Income",
                icon = Icons.Filled.TrendingUp,
                iconTint = if (state.monthlyIncome - state.monthlyExpenses >= 0) StatusOnline else StatusError,
                value = formatCurrency(state.monthlyIncome - state.monthlyExpenses),
                subtitle = "this month",
                onClick = { onNavigate("expenses") },
            )
        }

        // Recently Added to Plex
        if (state.recentlyAdded.isNotEmpty()) {
            Text(
                "Recently Added to Plex",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp),
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.recentlyAdded) { item ->
                    Card(
                        modifier = Modifier
                            .width(140.dp)
                            .clickable { onNavigate("media") },
                    ) {
                        Column {
                            if (item.thumb != null) {
                                AsyncImage(
                                    model = "${BuildConfig.BASE_URL}${item.thumb}",
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                    }
                }
            }
        }

        // Power Health modal
        if (state.showPowerModal) {
            PowerHealthDialog(
                powerHealth = state.powerHealth,
                onDismiss = { viewModel.togglePowerModal() },
            )
        }
    }
}

@Composable
private fun DashboardTile(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    value: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PowerHealthDialog(
    powerHealth: com.morgans.dashboard.data.model.PowerHealthResponse?,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Power Health") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                powerHealth?.let { health ->
                    Text("Status: ${health.status}")
                    Text("Sensors: ${health.sensorCount}")

                    health.stats24h?.let { stats ->
                        Text(
                            "24h Stats",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column { Text("Min"); Text("%.1fV".format(stats.min ?: 0.0)) }
                            Column { Text("Avg"); Text("%.1fV".format(stats.avg ?: 0.0)) }
                            Column { Text("Max"); Text("%.1fV".format(stats.max ?: 0.0)) }
                        }
                    }

                    if (health.devices.isNotEmpty()) {
                        Text("Devices", style = MaterialTheme.typography.titleSmall)
                        health.devices.forEach { device ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(device.name, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    device.voltage?.let { "%.1fV".format(it) } ?: device.state,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}
