package com.morgans.dashboard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.morgans.dashboard.ui.screens.downloads.DownloadsScreen
import com.morgans.dashboard.ui.screens.expenses.ExpensesScreen
import com.morgans.dashboard.ui.screens.home.HomeScreen
import com.morgans.dashboard.ui.screens.logs.LogsScreen
import com.morgans.dashboard.ui.screens.media.MediaScreen
import com.morgans.dashboard.ui.screens.photos.PhotosScreen
import com.morgans.dashboard.ui.screens.settings.SettingsScreen
import com.morgans.dashboard.ui.screens.smarthome.SmartHomeScreen
import com.morgans.dashboard.ui.screens.users.UsersScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    onNavigate: (String) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onNavigate = onNavigate)
        }
        composable(Screen.SmartHome.route) {
            SmartHomeScreen()
        }
        composable(Screen.Photos.route) {
            PhotosScreen()
        }
        composable(Screen.Media.route) {
            MediaScreen()
        }
        composable(Screen.Downloads.route) {
            DownloadsScreen()
        }
        composable(Screen.Expenses.route) {
            ExpensesScreen()
        }
        composable(Screen.Logs.route) {
            LogsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.Users.route) {
            UsersScreen()
        }
    }
}
