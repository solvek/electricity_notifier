package com.solvek.electricitynotifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.solvek.electricitynotifier.EnApp.Companion.enApp
import com.solvek.electricitynotifier.EnWorker.Companion.schedulePeriodic
import com.solvek.electricitynotifier.ui.theme.ElectricityNotifierTheme

class MainActivity : ComponentActivity() {
    private val model = enApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        schedulePeriodic()
        model.log("Scheduled")
        setContent {
            val content by model.log.collectAsState()
            ElectricityNotifierTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LogContent(
                        content = content,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LogContent(content: String, modifier: Modifier = Modifier) {
    Text(
        text = content,
        modifier = modifier
            .verticalScroll(rememberScrollState())
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ElectricityNotifierTheme {
        LogContent("Android")
    }
}