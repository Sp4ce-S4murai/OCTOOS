package com.pedro.octoos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoDanger
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OctoTile

/** Barra de status: coordenadas à esquerda, temperaturas à direita. Valores reais ou "--". */
@Composable
fun OctoStatusStrip(
    coordsText: String,
    coordsActive: Boolean,
    tempText: String,
    tempHot: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(OctoTile)
            .border(1.dp, OctoBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = coordsText,
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.4.sp,
            color = if (coordsActive) OctoLime else OctoGray,
            maxLines = 1
        )
        Text(
            text = tempText,
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.4.sp,
            color = if (tempHot) OctoDanger else OctoText,
            maxLines = 1
        )
    }
}
