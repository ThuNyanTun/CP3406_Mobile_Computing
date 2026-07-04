package com.example.gameplayerglance.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

private enum class Destination(val label: String) {
    HOME("Glance"),
    SETTINGS("Settings")
}

/**
 * Main scaffold. Toggles between UtilityScreen (the glance view) and
 * SettingsScreen using a bottom navigation bar, all within a single Activity.
 */
@Composable
fun UtilityApp(viewModel: UtilityViewModel) {
    var currentDestination by rememberSaveable { mutableStateOf(Destination.HOME) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination == Destination.HOME,
                    onClick = { currentDestination = Destination.HOME },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Glance") },
                    label = { Text(Destination.HOME.label) }
                )
                NavigationBarItem(
                    selected = currentDestination == Destination.SETTINGS,
                    onClick = { currentDestination = Destination.SETTINGS },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text(Destination.SETTINGS.label) }
                )
            }
        }
    ) { innerPadding ->
        when (currentDestination) {
            Destination.HOME -> UtilityScreen(
                uiState = uiState,
                onRefresh = viewModel::refreshAll,
                onToggleSort = viewModel::toggleSortMode,
                modifier = androidx.compose.ui.Modifier.padding(innerPadding)
            )
            Destination.SETTINGS -> SettingsScreen(
                uiState = uiState,
                onToggleGame = viewModel::toggleGameSelected,
                onIntervalChange = viewModel::setRefreshInterval,
                onSearchQueryChange = viewModel::setSettingsSearchQuery,
                onAddCustomGame = viewModel::addCustomGame,
                onRemoveCustomGame = viewModel::removeCustomGame,
                onClearAddGameError = viewModel::clearAddGameError,
                modifier = androidx.compose.ui.Modifier.padding(innerPadding)
            )
        }
    }
}
