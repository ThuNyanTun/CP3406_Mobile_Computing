package com.example.gameplayerglance.data

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Response shape for Steam's GetNumberOfCurrentPlayers endpoint:
 * https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=570&format=json
 *
 * Example response:
 * { "response": { "player_count": 123456, "result": 1 } }
 */
data class PlayerCountResponse(
    val response: PlayerCountBody
)

data class PlayerCountBody(
    val player_count: Int?,
    val result: Int
)

interface SteamApiService {
    @GET("ISteamUserStats/GetNumberOfCurrentPlayers/v1/")
    suspend fun getCurrentPlayers(
        @Query("appid") appId: Int,
        @Query("format") format: String = "json"
    ): PlayerCountResponse
}
