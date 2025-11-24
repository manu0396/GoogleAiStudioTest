package com.example.googleaistudiotest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.googleaistudiotest.ui.screens.DeveloperAssistantScreen
import com.example.googleaistudiotest.ui.theme.GoogleAiStudioTestTheme
import com.example.project.shared.contract.KtorAiService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the Service
        val aiService = KtorAiService(BuildConfig.API_KEY)

        enableEdgeToEdge()
        setContent {
            GoogleAiStudioTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 2. Replace Greeting(...) with FinancialAnalyzerScreen
                    // Pass the Modifier to respect system bars (edge-to-edge)
                    // Pass the aiService so the UI can call generateContent()
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        DeveloperAssistantScreen(aiService)
                    }
                }
            }
        }
    }
}
