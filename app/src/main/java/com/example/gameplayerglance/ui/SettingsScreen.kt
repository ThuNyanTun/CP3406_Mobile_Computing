package com.example.gameplayerglance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gameplayerglance.data.Game

/**
 * Settings screen: controls which games appear on the main glance screen,
 * how often they auto-refresh, plus search and the ability to add any
 * Steam game by App ID. Intentionally not persisted (in-memory only),
 * per the assignment brief.
 */
@Composable
fun SettingsScreen(
    uiState: UtilityUiState,
    onToggleGame: (Int) -> Unit,
    onIntervalChange: (RefreshInterval) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddCustomGame: (String, String) -> Unit,
    onRemoveCustomGame: (Int) -> Unit,
    onClearAddGameError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Refresh interval", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        RefreshIntervalDropdown(
            current = uiState.refreshInterval,
            onIntervalChange = onIntervalChange
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        AddGameForm(
            errorMessage = uiState.addGameError,
            onAddCustomGame = onAddCustomGame,
            onClearError = onClearAddGameError
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Games to track", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Choose which games show on the Glance screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.settingsSearchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search your games") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.settingsSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.filteredGames.isEmpty()) {
            Text(
                "No games match \"${uiState.settingsSearchQuery}\".",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            LazyColumn {
                items(uiState.filteredGames, key = { it.appId }) { game ->
                    GameToggleRow(
                        game = game,
                        checked = game.appId in uiState.selectedGameIds,
                        onToggle = { onToggleGame(game.appId) },
                        onRemove = { onRemoveCustomGame(game.appId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddGameForm(
    errorMessage: String?,
    onAddCustomGame: (String, String) -> Unit,
    onClearError: () -> Unit
) {
    var appIdInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    Text("Track any Steam game", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "Find the App ID in the game's Steam store URL, e.g. store.steampowered.com/app/730 → 730.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        OutlinedTextField(
            value = appIdInput,
            onValueChange = {
                appIdInput = it
                if (errorMessage != null) onClearError()
            },
            modifier = Modifier.width(110.dp),
            label = { Text("App ID") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            isError = errorMessage != null
        )
        OutlinedTextField(
            value = nameInput,
            onValueChange = {
                nameInput = it
                if (errorMessage != null) onClearError()
            },
            modifier = Modifier.weight(1f),
            label = { Text("Name (optional)") },
            singleLine = true
        )
        IconButton(
            onClick = {
                if (appIdInput.isNotBlank()) {
                    onAddCustomGame(appIdInput, nameInput)
                    appIdInput = ""
                    nameInput = ""
                }
            }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add game")
        }
    }

    if (errorMessage != null) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun GameToggleRow(
    game: Game,
    checked: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(game.displayName, style = MaterialTheme.typography.bodyLarge)
            if (game.isCustom) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "custom",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (game.isCustom) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remove ${game.displayName}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun RefreshIntervalDropdown(
    current: RefreshInterval,
    onIntervalChange: (RefreshInterval) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text(current.label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            RefreshInterval.values().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onIntervalChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
