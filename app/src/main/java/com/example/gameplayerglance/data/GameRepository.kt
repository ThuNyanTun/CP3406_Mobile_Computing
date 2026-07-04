package com.example.gameplayerglance.data

/**
 * Repository pattern: the ViewModel talks to this abstraction, not to Retrofit directly.
 * Makes it easy to swap in a fake implementation for testing/previews.
 */
interface GameRepository {
    suspend fun fetchPlayerCount(game: Game): Result<Int>
}

class SteamGameRepository(
    private val api: SteamApiService
) : GameRepository {

    override suspend fun fetchPlayerCount(game: Game): Result<Int> {
        return try {
            val response = api.getCurrentPlayers(appId = game.appId)
            val count = response.response.player_count
            if (response.response.result == 1 && count != null) {
                Result.success(count)
            } else {
                Result.failure(Exception("Steam API returned no data for ${game.displayName}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
