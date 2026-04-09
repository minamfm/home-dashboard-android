package com.morgans.dashboard.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val adminOnly: Boolean = false,
) {
    data object Home : Screen("home", "Dashboard", Icons.Outlined.Dashboard, Icons.Filled.Dashboard)
    data object SmartHome : Screen("smarthome", "Smart Home", Icons.Outlined.Home, Icons.Filled.Home)
    data object Photos : Screen("photos", "Photos", Icons.Outlined.Photo, Icons.Filled.Photo)
    data object Media : Screen("media", "Media", Icons.Outlined.Movie, Icons.Filled.Movie)
    data object Downloads : Screen("downloads", "Downloads", Icons.Outlined.Download, Icons.Filled.Download)
    data object Expenses : Screen("expenses", "Expenses", Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet)
    data object Logs : Screen("logs", "Activity", Icons.Outlined.Timeline, Icons.Filled.Timeline)
    data object Settings : Screen("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
    data object Users : Screen("users", "Users", Icons.Outlined.People, Icons.Filled.People, adminOnly = true)

    companion object {
        val allScreens = listOf(Home, SmartHome, Photos, Media, Downloads, Expenses, Logs, Settings, Users)
        val navItems = allScreens.filter { it != Users }
    }
}
