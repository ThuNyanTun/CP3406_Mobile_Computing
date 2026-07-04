package com.example.gameplayerglance.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gameplayerglance.data.GameStatus
import com.example.gameplayerglance.data.Trend
import com.example.gameplayerglance.ui.theme.TrendDownRed
import com.example.gameplayerglance.ui.theme.TrendFlatGray
import com.example.gameplayerglance.ui.theme.TrendUpGreen
import java.text.NumberFormat

/**
 * The core "at-a-glance" utility screen: live player counts for the
 * user's selected games, refreshed automatically per the settings screen.
 */
@Composable
fun UtilityScreen(
    uiState: UtilityUiState,
    onRefresh: () -> Unit,
    onToggleSort: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Player Counts", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = uiState.lastUpdatedLabel?.let { "Updated $it" }
                        ?: "Auto-refresh: ${uiState.refreshInterval.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onToggleSort) {
                    Icon(
                        imageVector = if (uiState.sortMode == SortMode.DEFAULT) {
                            Icons.Filled.SortByAlpha
                        } else {
                            Icons.Filled.TrendingUp
                        },
                        contentDescription = "Toggle sort order: ${uiState.sortMode.label}",
                        tint = if (uiState.sortMode == SortMode.PLAYERS_DESC) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh now")
                }
            }
        }

        if (uiState.statuses.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    TotalPlayersSummary(
                        totalPlayers = uiState.totalPlayers,
                        gameCount = uiState.statuses.size,
                        sortMode = uiState.sortMode
                    )
                }
                items(uiState.sortedStatuses, key = { it.game.appId }) { status ->
                    GameStatusCard(status)
                }
            }
        }
    }
}

@Composable
private fun TotalPlayersSummary(totalPlayers: Int?, gameCount: Int, sortMode: SortMode) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Combined players online",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = totalPlayers?.let { NumberFormat.getIntegerInstance().format(it) } ?: "--",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = "$gameCount tracked · ${sortMode.label}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.SportsEsports,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "No games selected. Add some in Settings.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GameStatusCard(status: GameStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(status.game.displayName, style = MaterialTheme.typography.titleMedium)
                if (status.error != null) {
                    Text(
                        text = "Unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (status.peakCount != null) {
                    Text(
                        text = "Session peak: ${NumberFormat.getIntegerInstance().format(status.peakCount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when {
                status.isLoading && status.playerCount == null -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                status.playerCount != null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrendIcon(status.trend)
                        Spacer(modifier = Modifier.width(6.dp))
                        AnimatedContent(
                            targetState = status.playerCount,
                            transitionSpec = {
                                (slideInVertically { height -> height } )
                                    .togetherWith(slideOutVertically { height -> -height })
                            },
                            label = "playerCount"
                        ) { count ->
                            Text(
                                text = NumberFormat.getIntegerInstance().format(count),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
                else -> {
                    Text("--", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
private fun TrendIcon(trend: Trend) {
    val (icon, tint) = when (trend) {
        Trend.UP -> Icons.Filled.ArrowUpward to TrendUpGreen
        Trend.DOWN -> Icons.Filled.ArrowDownward to TrendDownRed
        Trend.FLAT -> Icons.Filled.Remove to TrendFlatGray
    }
    Icon(icon, contentDescription = trend.name, tint = tint, modifier = Modifier.size(18.dp))
}
