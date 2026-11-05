package com.pedro.octoos.ui.clock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.ui.components.CyberBackground
import com.pedro.octoos.ui.components.OrbitClock
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime

/** Tela de relógio em tela cheia: o mesmo anel orbital da home, ampliado. */
@Composable
fun ClockScreen(
    hours24: Int,
    minutes: Int,
    seconds: Int,
    dateLabel: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    CyberBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "< VOLTAR",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = OctoLime,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBackClick)
                    .padding(vertical = 6.dp)
            )
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val ring = minOf(maxWidth, maxHeight) * 0.92f
                OrbitClock(
                    hours24 = hours24,
                    minutes = minutes,
                    seconds = seconds,
                    dateLabel = dateLabel,
                    ringSize = ring,
                    orbitMode = true,
                    animationsEnabled = true
                )
            }
        }
    }
}
