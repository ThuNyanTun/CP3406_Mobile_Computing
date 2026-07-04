package com.example.gameplayerglance

import android.app.Application
import com.example.gameplayerglance.di.AppContainer
import com.example.gameplayerglance.di.DefaultAppContainer

class GameGlanceApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
