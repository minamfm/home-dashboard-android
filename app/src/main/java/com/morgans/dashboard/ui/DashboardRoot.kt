package com.morgans.dashboard.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.morgans.dashboard.ui.navigation.NavGraph
import com.morgans.dashboard.ui.navigation.Screen
import com.morgans.dashboard.ui.screens.chatbot.ChatbotSheet
import com.morgans.dashboard.ui.screens.login.LoginScreen
import com.morgans.dashboard.ui.screens.login.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardRoot(
    loginViewModel: LoginViewModel = hiltViewModel(),
) {
    val isLoggedIn by loginViewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
    val isAdmin by loginViewModel.isAdmin.collectAsStateWithLifecycle(initialValue = false)

    when (isLoggedIn) {
        null -> {
            // Loading state
            Box(Modifier.fillMaxSize())
        }
        false -> {
            LoginScreen(viewModel = loginViewModel)
        }
        true -> {
            DashboardScaffold(isAdmin = isAdmin)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScaffold(isAdmin: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    var showChatbot by remember { mutableStateOf(false) }

    val navItems = Screen.navItems.let { items ->
        if (isAdmin) items + Screen.Users else items
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.icon,
                                contentDescription = screen.title,
                            )
                        },
                        label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showChatbot = true },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Filled.SmartToy, contentDescription = "Chat")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavGraph(
                navController = navController,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }

    if (showChatbot) {
        ChatbotSheet(onDismiss = { showChatbot = false })
    }
}
