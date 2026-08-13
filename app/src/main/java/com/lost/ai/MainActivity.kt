package com.lost.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lost.ai.ui.screens.ChatScreen
import com.lost.ai.ui.theme.LostAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LostAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LostAITheme.colors.background
                ) {
                    ChatScreen()
                }
            }
        }
    }
}