package com.pedro.octoos.ui.update

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OctoTile
import com.pedro.octoos.ui.theme.OrbitronFamily
import com.pedro.octoos.update.UpdateReleaseInfo

/**
 * Full-screen update view matching the "4. Atualização" layout:
 * - Header: ATUALIZAÇÃO | 1.3 → 1.4
 * - Title: VERSÃO 1.4
 * - O QUE MUDOU with "> " bullets
 * - Progress bar + BAIXANDO | 64%
 * - Bottom buttons: INSTALAR (lime) and MAIS TARDE (dark outlined)
 */
@Composable
fun UpdateScreen(
    releaseInfo: UpdateReleaseInfo,
    downloadProgressPercent: Int,
    onInstallClick: () -> Unit,
    onLaterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OctoBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        UpdateTopBar(
            releaseInfo = releaseInfo,
            onBackClick = onLaterClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "O QUE MUDOU",
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = OctoLime,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            releaseInfo.changelogItems.forEach { bullet ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "> ",
                        fontFamily = IbmPlexMonoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = OctoLime
                    )
                    Text(
                        text = bullet,
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 12.sp,
                        color = OctoText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Progress bar
        val progressFraction = (downloadProgressPercent.coerceIn(0, 100) / 100f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(OctoTile)
                .border(1.dp, OctoBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .fillMaxHeight()
                    .background(OctoLime)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (downloadProgressPercent >= 100) "PRONTO PARA INSTALAR" else "BAIXANDO",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoGray,
                letterSpacing = 0.6.sp
            )
            Text(
                text = "${downloadProgressPercent.coerceIn(0, 100)}%",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoGray
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .background(OctoLime)
                    .clickable(onClick = onInstallClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "INSTALAR",
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = OctoBackground,
                    letterSpacing = 0.8.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .background(OctoBackground)
                    .border(1.dp, OctoBorder)
                    .clickable(onClick = onLaterClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MAIS TARDE",
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = OctoText,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun UpdateTopBar(
    releaseInfo: UpdateReleaseInfo,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ATUALIZAÇÃO",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime,
                letterSpacing = 0.6.sp,
                modifier = Modifier.clickable(onClick = onBackClick)
            )
            Text(
                text = "${releaseInfo.currentVersionLabel} → ${releaseInfo.targetVersionLabel}",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoGray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OctoBorder)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = OctoText, fontWeight = FontWeight.Bold)) {
                    append("VERSÃO ")
                }
                withStyle(SpanStyle(color = OctoLime, fontWeight = FontWeight.Bold)) {
                    append(releaseInfo.targetVersionLabel)
                }
            },
            fontFamily = OrbitronFamily,
            fontSize = 22.sp,
            letterSpacing = 1.5.sp
        )
    }
}

@Preview(
    name = "Atualização — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun UpdateScreenPreview() {
    OctoOSTheme {
        UpdateScreen(
            releaseInfo = UpdateReleaseInfo(),
            downloadProgressPercent = 64,
            onInstallClick = {},
            onLaterClick = {}
        )
    }
}
