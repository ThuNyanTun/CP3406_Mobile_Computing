package com.example.gameplayerglance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gameplayerglance.ui.UtilityApp
import com.example.gameplayerglance.ui.UtilityViewModel
import com.example.gameplayerglance.ui.theme.GamePlayerGlanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as GameGlanceApplication

        setContent {
            GamePlayerGlanceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: UtilityViewModel = viewModel(
                        factory = UtilityViewModel.provideFactory(app.container.gameRepository)
                    )
                    UtilityApp(viewModel = viewModel)
                }
            }
        }
    }
}
