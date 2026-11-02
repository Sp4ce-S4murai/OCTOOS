package com.pedro.octoos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pedro.octoos.ui.components.OrbitClock
import com.pedro.octoos.ui.components.formatPortugueseShortDate
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoOSTheme
import kotlinx.coroutines.delay
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OctoOSTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OctoBackground),
                    containerColor = OctoBackground
                ) { innerPadding ->
                    ClockHostBox(
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
private fun ClockHostBox(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000L)
        }
    }

    Box(
        modifier = modifier.background(OctoBackground),
        contentAlignment = Alignment.Center
    ) {
        OrbitClock(
            hours24 = now.hour,
            minutes = now.minute,
            seconds = now.second,
            dateLabel = formatPortugueseShortDate(now)
        )
    }
}

