package com.morgans.dashboard.ui.screens.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.morgans.dashboard.data.model.User
import com.morgans.dashboard.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UsersState(
    val users: List<User> = emptyList(),
    val loading: Boolean = true,
    val showCreateForm: Boolean = false,
)

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UsersState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val users = repository.getUsers()
                _state.update { it.copy(users = users, loading = false) }
            } catch (_: Exception) {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    fun createUser(username: String, password: String, role: String) {
        viewModelScope.launch {
            try {
                repository.createUser(username, password, role)
                _state.update { it.copy(showCreateForm = false) }
                load()
            } catch (_: Exception) {}
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteUser(id)
                load()
            } catch (_: Exception) {}
        }
    }

    fun toggleRole(user: User) {
        val newRole = if (user.role == "admin") "member" else "admin"
        viewModelScope.launch {
            try {
                repository.updateUserRole(user.id, newRole)
                load()
            } catch (_: Exception) {}
        }
    }

    fun toggleCreateForm() {
        _state.update { it.copy(showCreateForm = !it.showCreateForm) }
    }
}

@Composable
fun UsersScreen(viewModel: UsersViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newRole by remember { mutableStateOf("member") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Users", style = MaterialTheme.typography.headlineLarge)
                IconButton(onClick = { viewModel.toggleCreateForm() }) {
                    Icon(Icons.Filled.PersonAdd, "Add user")
                }
            }
        }

        // Create form
        if (state.showCreateForm) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Create User", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = newUsername,
                            onValueChange = { newUsername = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = newRole == "member",
                                onClick = { newRole = "member" },
                                label = { Text("Member") },
                            )
                            FilterChip(
                                selected = newRole == "admin",
                                onClick = { newRole = "admin" },
                                label = { Text("Admin") },
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.createUser(newUsername, newPassword, newRole)
                                newUsername = ""
                                newPassword = ""
                                newRole = "member"
                            },
                            enabled = newUsername.isNotBlank() && newPassword.isNotBlank(),
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }

        if (state.loading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(state.users, key = { it.id }) { user ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(user.username, style = MaterialTheme.typography.titleSmall)
                            Text(
                                user.role.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row {
                            TextButton(onClick = { viewModel.toggleRole(user) }) {
                                Text(if (user.role == "admin") "Demote" else "Promote")
                            }
                            IconButton(onClick = { viewModel.deleteUser(user.id) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
