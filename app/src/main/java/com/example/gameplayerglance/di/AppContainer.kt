package com.example.gameplayerglance.di

import com.example.gameplayerglance.data.GameRepository
import com.example.gameplayerglance.data.SteamApiService
import com.example.gameplayerglance.data.SteamGameRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Lightweight, hand-rolled dependency injection container (no Hilt/Dagger needed
 * for a single-screen-graph utility app). Exposes ready-to-use singletons.
 */
interface AppContainer {
    val gameRepository: GameRepository
}

class DefaultAppContainer : AppContainer {

    private val baseUrl = "https://api.steampowered.com/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val steamApiService: SteamApiService by lazy {
        retrofit.create(SteamApiService::class.java)
    }

    override val gameRepository: GameRepository by lazy {
        SteamGameRepository(steamApiService)
    }
}
