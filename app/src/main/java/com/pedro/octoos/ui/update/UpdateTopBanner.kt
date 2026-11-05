package com.pedro.octoos.ui.update

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import com.pedro.octoos.update.UpdateReleaseInfo

/**
 * Top update alert banner displayed on HomeScreen when a newer GitHub Release versionCode exists.
 * Actions: "BAIXAR" (opens full-screen UpdateScreen) and "DEPOIS" (dismisses banner).
 */
@Composable
fun UpdateTopBanner(
    releaseInfo: UpdateReleaseInfo,
    onDownloadClick: () -> Unit,
    onLaterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(OctoTile)
            .border(1.dp, OctoLime)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "NOVA VERSÃO ${releaseInfo.targetVersionLabel}",
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = OctoText,
            letterSpacing = 0.6.sp,
            modifier = Modifier.weight(1f)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(OctoLime)
                    .clickable(onClick = onDownloadClick)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BAIXAR",
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = OctoBackground
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .background(OctoBackground)
                    .border(1.dp, OctoBorder)
                    .clickable(onClick = onLaterClick)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DEPOIS",
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 10.sp,
                    color = OctoGray
                )
            }
        }
    }
}

@Preview(
    name = "Update Top Banner",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A
)
@Composable
private fun UpdateTopBannerPreview() {
    OctoOSTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            UpdateTopBanner(
                releaseInfo = UpdateReleaseInfo(),
                onDownloadClick = {},
                onLaterClick = {}
            )
        }
    }
}

