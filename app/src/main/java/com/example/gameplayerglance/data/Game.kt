package com.example.gameplayerglance.data

/**
 * Static catalogue entry for a game we can query on Steam.
 * appId is Steam's unique identifier for the game.
 */
data class Game(
    val appId: Int,
    val displayName: String,
    /** True if the user added this game manually (rather than the built-in catalogue). */
    val isCustom: Boolean = false
)

/**
 * The result of fetching a live player count for a single game.
 * playerCount is null while loading or if the fetch failed.
 */
data class GameStatus(
    val game: Game,
    val playerCount: Int? = null,
    val previousCount: Int? = null,
    /** Highest player count seen for this game during the current app session. */
    val peakCount: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /** Simple trend indicator compared to the previous successful fetch. */
    val trend: Trend
        get() {
            val current = playerCount
            val previous = previousCount
            if (current == null || previous == null) return Trend.FLAT
            return when {
                current > previous -> Trend.UP
                current < previous -> Trend.DOWN
                else -> Trend.FLAT
            }
        }
}

enum class Trend { UP, DOWN, FLAT }

/** The fixed catalogue of games the app knows how to look up. */
object GameCatalogue {
    val allGames = listOf(
        Game(appId = 570, displayName = "Dota 2"),
        Game(appId = 730, displayName = "Counter-Strike 2"),
        Game(appId = 578080, displayName = "PUBG: Battlegrounds"),
        Game(appId = 1172470, displayName = "Apex Legends"),
        Game(appId = 440, displayName = "Team Fortress 2"),
        Game(appId = 1091500, displayName = "Cyberpunk 2077")
    )
}
