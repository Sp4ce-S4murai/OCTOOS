package com.pedro.octoos.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.data.MediaInfo
import com.pedro.octoos.ui.components.pressScale
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OctoTile

@Composable
fun NowPlayingBar(
    info: MediaInfo,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(OctoTile)
            .border(1.dp, OctoBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "♪ ${info.appLabel}",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 9.sp,
                color = OctoLime,
                maxLines = 1
            )
            Text(
                text = info.title.ifBlank { "SEM TÍTULO" },
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = OctoText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (info.artist.isNotBlank()) {
                Text(
                    text = info.artist,
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 11.sp,
                    color = OctoGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            MediaKey(label = "<<", accent = false, onClick = onPrevious)
            MediaKey(label = if (info.isPlaying) "||" else ">", accent = true, onClick = onPlayPause)
            MediaKey(label = ">>", accent = false, onClick = onNext)
        }
    }
}

@Composable
private fun MediaKey(label: String, accent: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(start = 6.dp)
            .size(width = 40.dp, height = 36.dp)
            .pressScale()
            .background(if (accent) OctoLime else OctoBackground)
            .border(1.dp, OctoLime)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (accent) OctoBackground else OctoLime
        )
    }
}
