package com.example.gameplayerglance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gameplayerglance.data.Game
import com.example.gameplayerglance.data.GameCatalogue
import com.example.gameplayerglance.data.GameRepository
import com.example.gameplayerglance.data.GameStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RefreshInterval(val seconds: Long, val label: String) {
    FIFTEEN(15, "15 seconds"),
    THIRTY(30, "30 seconds"),
    SIXTY(60, "60 seconds"),
    MANUAL(0, "Manual only")
}

enum class SortMode(val label: String) {
    DEFAULT("Default order"),
    PLAYERS_DESC("Most players first")
}

/**
 * Everything the UI needs, in one place. Settings here are intentionally
 * in-memory only (not persisted), per the assignment's requirements.
 */
data class UtilityUiState(
    val allGames: List<Game> = GameCatalogue.allGames,
    val selectedGameIds: Set<Int> = GameCatalogue.allGames.take(4).map { it.appId }.toSet(),
    val refreshInterval: RefreshInterval = RefreshInterval.THIRTY,
    val statuses: List<GameStatus> = emptyList(),
    val lastUpdatedLabel: String? = null,
    val sortMode: SortMode = SortMode.DEFAULT,
    val settingsSearchQuery: String = "",
    val addGameError: String? = null
) {
    /** Statuses in the order/sort the Glance screen should render them. */
    val sortedStatuses: List<GameStatus>
        get() = when (sortMode) {
            SortMode.DEFAULT -> statuses
            SortMode.PLAYERS_DESC -> statuses.sortedByDescending { it.playerCount ?: -1 }
        }

    /** Sum of all successfully-loaded player counts currently shown. */
    val totalPlayers: Int?
        get() {
            val counts = statuses.mapNotNull { it.playerCount }
            return if (counts.isEmpty()) null else counts.sum()
        }

    /** Games in the catalogue filtered by the Settings search box. */
    val filteredGames: List<Game>
        get() = if (settingsSearchQuery.isBlank()) {
            allGames
        } else {
            allGames.filter { it.displayName.contains(settingsSearchQuery, ignoreCase = true) }
        }
}

class UtilityViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UtilityUiState())
    val uiState: StateFlow<UtilityUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: kotlinx.coroutines.Job? = null

    init {
        refreshAll()
        startAutoRefreshLoop()
    }

    /** Toggle whether a game shows up on the main glance screen. */
    fun toggleGameSelected(appId: Int) {
        _uiState.update { state ->
            val newSelection = if (appId in state.selectedGameIds) {
                state.selectedGameIds - appId
            } else {
                state.selectedGameIds + appId
            }
            state.copy(selectedGameIds = newSelection)
        }
        refreshAll()
    }

    fun setRefreshInterval(interval: RefreshInterval) {
        _uiState.update { it.copy(refreshInterval = interval) }
        startAutoRefreshLoop()
    }

    fun setSortMode(mode: SortMode) {
        _uiState.update { it.copy(sortMode = mode) }
    }

    fun toggleSortMode() {
        _uiState.update {
            it.copy(
                sortMode = if (it.sortMode == SortMode.DEFAULT) {
                    SortMode.PLAYERS_DESC
                } else {
                    SortMode.DEFAULT
                }
            )
        }
    }

    fun setSettingsSearchQuery(query: String) {
        _uiState.update { it.copy(settingsSearchQuery = query) }
    }

    /**
     * Add a game the user looked up by Steam App ID. Validates the ID, checks
     * for duplicates, then adds it to the catalogue and selects it automatically.
     */
    fun addCustomGame(appIdText: String, displayNameInput: String) {
        val appId = appIdText.trim().toIntOrNull()
        if (appId == null || appId <= 0) {
            _uiState.update { it.copy(addGameError = "Enter a valid numeric Steam App ID") }
            return
        }
        val state = _uiState.value
        if (state.allGames.any { it.appId == appId }) {
            _uiState.update { it.copy(addGameError = "That game is already in your list") }
            return
        }
        val name = displayNameInput.trim().ifBlank { "App $appId" }
        val newGame = Game(appId = appId, displayName = name, isCustom = true)

        _uiState.update {
            it.copy(
                allGames = it.allGames + newGame,
                selectedGameIds = it.selectedGameIds + appId,
                addGameError = null
            )
        }
        refreshAll()
    }

    fun clearAddGameError() {
        _uiState.update { it.copy(addGameError = null) }
    }

    /** Remove a user-added game entirely (built-in catalogue games can only be toggled off). */
    fun removeCustomGame(appId: Int) {
        _uiState.update { state ->
            state.copy(
                allGames = state.allGames.filterNot { it.appId == appId && it.isCustom },
                selectedGameIds = state.selectedGameIds - appId,
                statuses = state.statuses.filterNot { it.game.appId == appId }
            )
        }
    }

    /** Manual pull-to-refresh / refresh button entry point. */
    fun refreshAll() {
        viewModelScope.launch {
            val state = _uiState.value
            val selectedGames = state.allGames.filter { it.appId in state.selectedGameIds }

            // Mark everything currently selected as loading.
            _uiState.update { current ->
                current.copy(
                    statuses = selectedGames.map { game ->
                        current.statuses.find { it.game.appId == game.appId }
                            ?.copy(isLoading = true)
                            ?: GameStatus(game = game, isLoading = true)
                    }
                )
            }

            selectedGames.forEach { game ->
                launch {
                    val result = repository.fetchPlayerCount(game)
                    _uiState.update { current ->
                        val previous = current.statuses.find { it.game.appId == game.appId }
                        val newStatus = result.fold(
                            onSuccess = { count ->
                                val peak = maxOf(count, previous?.peakCount ?: count)
                                GameStatus(
                                    game = game,
                                    playerCount = count,
                                    previousCount = previous?.playerCount,
                                    peakCount = peak,
                                    isLoading = false,
                                    error = null
                                )
                            },
                            onFailure = { e ->
                                GameStatus(
                                    game = game,
                                    playerCount = previous?.playerCount,
                                    previousCount = previous?.previousCount,
                                    peakCount = previous?.peakCount,
                                    isLoading = false,
                                    error = e.message ?: "Failed to load"
                                )
                            }
                        )
                        current.copy(
                            statuses = current.statuses.map {
                                if (it.game.appId == game.appId) newStatus else it
                            },
                            lastUpdatedLabel = "Just now"
                        )
                    }
                }
            }
        }
    }

    private fun startAutoRefreshLoop() {
        autoRefreshJob?.cancel()
        val interval = _uiState.value.refreshInterval
        if (interval == RefreshInterval.MANUAL) return

        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(interval.seconds * 1000)
                refreshAll()
            }
        }
    }

    companion object {
        fun provideFactory(repository: GameRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return UtilityViewModel(repository) as T
                }
            }
    }
}
