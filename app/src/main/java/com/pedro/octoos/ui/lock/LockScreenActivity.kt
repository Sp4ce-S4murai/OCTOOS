package com.pedro.octoos.ui.lock

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.data.LauncherPreferencesRepository
import com.pedro.octoos.data.MediaSessionRepository
import com.pedro.octoos.data.WeatherState
import com.pedro.octoos.ui.components.CyberBackground
import com.pedro.octoos.ui.components.formatPortugueseShortDate
import com.pedro.octoos.ui.media.NowPlayingBar
import com.pedro.octoos.ui.theme.BebasNeueFamily
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * Tela de bloqueio do OctoOS: relógio, clima, mídia tocando e botão de desbloqueio.
 * Aparece por cima da tela de bloqueio do Android quando aberta (setShowWhenLocked),
 * mas não substitui o bloqueio do sistema: o Android não permite isso a um launcher comum.
 */
class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        enableEdgeToEdge()
        setContent {
            OctoOSTheme {
                LockScreenContent(onUnlock = { finish() })
            }
        }
    }
}

@Composable
private fun LockScreenContent(onUnlock: () -> Unit) {
    val context = LocalContext.current
    val prefsRepo = remember { LauncherPreferencesRepository(context) }
    val weather by prefsRepo.weatherFlow.collectAsState(initial = WeatherState())
    val nowPlaying by MediaSessionRepository.state.collectAsState()
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000L)
        }
    }

    CyberBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = weather.temperatureCelsius?.let { "EXT $it°C" } ?: "EXT --°C",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 12.sp,
                color = OctoGray
            )

            Spacer(modifier = Modifier.weight(1f))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val timeSize = (maxWidth.value * 0.42f).coerceAtMost(200f).sp
                Text(
                    text = String.format("%02d:%02d", now.hour, now.minute),
                    fontFamily = BebasNeueFamily,
                    fontSize = timeSize,
                    color = OctoLime
                )
            }

            Text(
                text = formatPortugueseShortDate(now),
                fontFamily = IbmPlexMonoFamily,
                fontSize = 13.sp,
                color = OctoText
            )

            Spacer(modifier = Modifier.weight(1f))

            val playing = nowPlaying
            if (playing != null) {
                NowPlayingBar(
                    info = playing,
                    onPlayPause = { MediaSessionRepository.togglePlayPause() },
                    onNext = { MediaSessionRepository.next() },
                    onPrevious = { MediaSessionRepository.previous() }
                )
            } else {
                Text(
                    text = "SEM MÚSICA OU VÍDEO TOCANDO",
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 11.sp,
                    color = OctoGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OctoLime)
                    .clickable(onClick = onUnlock)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DESBLOQUEAR",
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = OctoBackground
                )
            }
        }
    }
}
