package com.pedro.octoos.ui.clock

import android.content.Intent
import android.provider.AlarmClock
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.ui.components.CyberBackground
import com.pedro.octoos.ui.components.pressScale
import com.pedro.octoos.ui.theme.BebasNeueFamily
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OctoTile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Interface de relógio do app: mostra hora e data e permite ajustar ano, mês, dia, hora e minuto
 * só para exibição (o relógio do aparelho não muda). O botão final abre o relógio do próprio aparelho.
 */
@Composable
fun ClockScreen(
    now: LocalDateTime,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    val context = LocalContext.current

    var yearsShift by remember { mutableIntStateOf(0) }
    var monthsShift by remember { mutableIntStateOf(0) }
    var daysShift by remember { mutableIntStateOf(0) }
    var hoursShift by remember { mutableIntStateOf(0) }
    var minutesShift by remember { mutableIntStateOf(0) }

    val shown = now
        .plusYears(yearsShift.toLong())
        .plusMonths(monthsShift.toLong())
        .plusDays(daysShift.toLong())
        .plusHours(hoursShift.toLong())
        .plusMinutes(minutesShift.toLong())
    val adjusted = yearsShift != 0 || monthsShift != 0 || daysShift != 0 || hoursShift != 0 || minutesShift != 0

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, dd 'DE' MMMM 'DE' yyyy", Locale("pt", "BR")) }

    CyberBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "< VOLTAR",
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = OctoLime,
                    modifier = Modifier
                        .clickable(onClick = onBackClick)
                        .padding(vertical = 6.dp)
                )
                Text(
                    text = if (adjusted) "AJUSTADO" else "HORA DO APARELHO",
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 10.sp,
                    color = if (adjusted) OctoLime else OctoGray
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = String.format("%02d:%02d", shown.hour, shown.minute),
                fontFamily = BebasNeueFamily,
                fontSize = 120.sp,
                color = OctoLime
            )
            Text(
                text = String.format(":%02d", now.second),
                fontFamily = IbmPlexMonoFamily,
                fontSize = 14.sp,
                color = OctoGray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = shown.format(dateFormatter).uppercase(Locale("pt", "BR")),
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = OctoText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "AJUSTAR DATA E HORA",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoGray,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            AdjustRow(
                label = "ANO",
                value = shown.year.toString(),
                onMinus = { yearsShift-- },
                onPlus = { yearsShift++ }
            )
            AdjustRow(
                label = "MÊS",
                value = String.format("%02d", shown.monthValue),
                onMinus = { monthsShift-- },
                onPlus = { monthsShift++ }
            )
            AdjustRow(
                label = "DIA",
                value = String.format("%02d", shown.dayOfMonth),
                onMinus = { daysShift-- },
                onPlus = { daysShift++ }
            )
            AdjustRow(
                label = "HORA",
                value = String.format("%02d", shown.hour),
                onMinus = { hoursShift-- },
                onPlus = { hoursShift++ }
            )
            AdjustRow(
                label = "MINUTO",
                value = String.format("%02d", shown.minute),
                onMinus = { minutesShift-- },
                onPlus = { minutesShift++ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressScale()
                    .border(1.dp, OctoBorder)
                    .clickable {
                        yearsShift = 0
                        monthsShift = 0
                        daysShift = 0
                        hoursShift = 0
                        minutesShift = 0
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RESTAURAR HORA DO APARELHO",
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = OctoText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressScale()
                    .background(OctoLime)
                    .clickable {
                        val openClock = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                        val opened = runCatching { context.startActivity(openClock) }.isSuccess
                        if (!opened) {
                            context.startActivity(Intent(Settings.ACTION_DATE_SETTINGS))
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ABRIR RELÓGIO DO APARELHO",
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = OctoBackground
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdjustRow(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = IbmPlexMonoFamily,
            fontSize = 11.sp,
            color = OctoGray,
            modifier = Modifier.weight(1f)
        )
        StepButton(label = "-", onClick = onMinus)
        Text(
            text = value,
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = OctoText,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(110.dp)
        )
        StepButton(label = "+", onClick = onPlus)
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .pressScale()
            .background(OctoTile)
            .border(1.dp, OctoLime)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = OctoLime
        )
    }
}
