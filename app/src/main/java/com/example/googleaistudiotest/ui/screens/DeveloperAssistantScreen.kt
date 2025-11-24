package com.example.googleaistudiotest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.shared.contract.AiService
import kotlinx.coroutines.launch

@Composable
fun DeveloperAssistantScreen(aiService: AiService) {
    var input by remember { mutableStateOf("How do I use Flow in ViewModel?") }
    var responses by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "KMP Architect Helper",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF006C4C) // Android Green
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Ask about Kotlin/Compose...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (input.isNotBlank()) {
                    isLoading = true
                    scope.launch {
                        val answer = aiService.generateText(input)
                        responses = listOf(input to answer) + responses
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006C4C))
        ) {
            Text(if (isLoading) "Generating Code..." else "Ask Architect")
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(responses) { (question, answer) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Q: $question", fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(text = answer) // This will now contain code snippets!
                    }
                }
            }
        }
    }
}
