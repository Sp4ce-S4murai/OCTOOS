package com.pedro.octoos.ui.allapps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.data.AppEntry
import com.pedro.octoos.data.InstalledAppsHelper
import com.pedro.octoos.ui.components.OctoIconType
import com.pedro.octoos.ui.components.OctoSolidIcon
import com.pedro.octoos.ui.theme.BebasNeueFamily
import com.pedro.octoos.ui.theme.ChamferedShape
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OrbitronFamily
import java.text.Normalizer
import java.util.Locale

enum class AllAppsFilter(val label: String) {
    AZ("A-Z"),
    USAGE("USO"),
    NEW("NOVOS")
}

@Composable
fun AllAppsScreen(
    apps: List<AppEntry>,
    dockApps: List<AppEntry>,
    onAppClick: (AppEntry) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialQuery: String = "",
    initialFilter: AllAppsFilter = AllAppsFilter.AZ
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedFilter by remember { mutableStateOf(initialFilter) }

    val filteredApps = remember(apps, searchQuery, selectedFilter) {
        val normalizedQuery = normalizeForSearch(searchQuery)
        val matching = if (normalizedQuery.isEmpty()) {
            apps
        } else {
            apps.filter { normalizeForSearch(it.label).contains(normalizedQuery) }
        }

        when (selectedFilter) {
            AllAppsFilter.AZ -> matching.sortedBy { normalizeForSearch(it.label) }
            AllAppsFilter.USAGE -> matching.sortedByDescending { it.launchCount }
            AllAppsFilter.NEW -> matching.sortedByDescending { it.firstInstallTime }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OctoBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        AllAppsTopBar(
            totalCount = apps.size.coerceAtLeast(24),
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(10.dp))

        SearchInputBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        FilterChipsRow(
            selected = selectedFilter,
            onSelect = { selectedFilter = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredApps.isEmpty()) {
            EmptyResultsState(modifier = Modifier.weight(1f))
        } else {
            when (selectedFilter) {
                AllAppsFilter.AZ -> AzSectionedGrid(
                    apps = filteredApps,
                    onAppClick = onAppClick,
                    modifier = Modifier.weight(1f)
                )

                AllAppsFilter.USAGE, AllAppsFilter.NEW -> AppUsageList(
                    apps = filteredApps,
                    onAppClick = onAppClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Compact icon-only lime dock at the bottom of All Apps
            CompactIconDock(
                dockApps = dockApps,
                onDockClick = { entry ->
                    if (entry.iconType == OctoIconType.TODOS) {
                        onBackClick()
                    } else {
                        onAppClick(entry)
                    }
                }
            )
        }
    }
}

@Composable
private fun AllAppsTopBar(
    totalCount: Int,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TODOS OS APPS",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime,
                letterSpacing = 0.6.sp,
                modifier = Modifier.clickable(onClick = onBackClick)
            )
            Text(
                text = "$totalCount",
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
                    append("APPS")
                }
                withStyle(SpanStyle(color = OctoLime, fontWeight = FontWeight.Bold)) {
                    append("/")
                }
            },
            fontFamily = OrbitronFamily,
            fontSize = 22.sp,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun SearchInputBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(OctoBackground)
            .border(width = 1.dp, color = OctoLime)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "> ",
            fontFamily = IbmPlexMonoFamily,
            fontSize = 13.sp,
            color = OctoLime
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = "buscar_",
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 13.sp,
                    color = OctoLime
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                cursorBrush = SolidColor(OctoLime),
                textStyle = TextStyle(
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 13.sp,
                    color = OctoLime
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FilterChipsRow(
    selected: AllAppsFilter,
    onSelect: (AllAppsFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AllAppsFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .background(if (isSelected) OctoLime else OctoBackground)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) OctoLime else OctoBorder
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.label,
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = if (isSelected) OctoBackground else OctoText,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun AzSectionedGrid(
    apps: List<AppEntry>,
    onAppClick: (AppEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val grouped = remember(apps) {
        apps.groupBy { entry ->
            val clean = normalizeForSearch(entry.label)
            clean.firstOrNull()?.uppercaseChar()?.toString() ?: "#"
        }.toSortedMap()
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        grouped.forEach { (letter, sectionApps) ->
            item(key = "header_$letter") {
                LetterSeparatorHeader(letter = letter)
            }

            // 3-column grid of lime chamfered tiles matching the mockup
            val rows = sectionApps.chunked(3)
            items(
                items = rows,
                key = { row -> "row_${letter}_${row.first().packageName}" }
            ) { rowApps ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0 until 3) {
                        val app = rowApps.getOrNull(col)
                        if (app != null) {
                            AllAppsLimeGridTile(
                                entry = app,
                                onClick = { onAppClick(app) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterSeparatorHeader(letter: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = letter,
            fontFamily = BebasNeueFamily,
            fontSize = 24.sp,
            color = OctoLime
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(OctoBorder)
        )
    }
}

@Composable
private fun AllAppsLimeGridTile(
    entry: AppEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = ChamferedShape(12.dp)
    Box(
        modifier = modifier
            .height(62.dp)
            .clip(shape)
            .background(OctoLime)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OctoSolidIcon(
                type = entry.iconType ?: OctoIconType.TODOS,
                size = 20.dp,
                tint = OctoBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.label,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 8.sp,
                color = OctoBackground,
                letterSpacing = 0.6.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AppUsageList(
    apps: List<AppEntry>,
    onAppClick: (AppEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        items(items = apps, key = { it.packageName }) { entry ->
            AppUsageListRow(
                entry = entry,
                onClick = { onAppClick(entry) }
            )
        }
    }
}

@Composable
private fun AppUsageListRow(
    entry: AppEntry,
    onClick: () -> Unit
) {
    val firstLetter = normalizeForSearch(entry.label).firstOrNull()?.toString() ?: "A"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = firstLetter,
                fontFamily = BebasNeueFamily,
                fontSize = 24.sp,
                color = OctoLime,
                modifier = Modifier.width(28.dp)
            )

            Text(
                text = entry.label,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = OctoText,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            // Open count on the right ("HOJE · 42×")
            Text(
                text = "${entry.recencyLabel} · ${entry.launchCount}×",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoGray,
                letterSpacing = 0.5.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OctoBorder)
        )
    }
}

@Composable
private fun CompactIconDock(
    dockApps: List<AppEntry>,
    onDockClick: (AppEntry) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        dockApps.take(4).forEach { dockItem ->
            val shape = ChamferedShape(12.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(shape)
                    .background(OctoLime)
                    .clickable { onDockClick(dockItem) },
                contentAlignment = Alignment.Center
            ) {
                OctoSolidIcon(
                    type = dockItem.iconType ?: OctoIconType.TODOS,
                    size = 20.dp,
                    tint = OctoBackground
                )
            }
        }
    }
}

@Composable
private fun EmptyResultsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SEM\nRESULTADOS",
                fontFamily = BebasNeueFamily,
                fontSize = 44.sp,
                lineHeight = 44.sp,
                color = OctoLime,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Nenhum app com esse nome.",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 11.sp,
                color = OctoGray
            )
        }
    }
}

private fun normalizeForSearch(input: String): String {
    val decomposed = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
    return decomposed
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .uppercase(Locale("pt", "BR"))
}

@Preview(
    name = "Todos os Apps (A–Z em Grade)",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun AllAppsAzPreview() {
    OctoOSTheme {
        AllAppsScreen(
            apps = InstalledAppsHelper.defaultCatalogApps(),
            dockApps = InstalledAppsHelper.defaultDockApps(),
            onAppClick = {},
            onBackClick = {},
            initialFilter = AllAppsFilter.AZ
        )
    }
}

@Preview(
    name = "Todos os Apps (USO em Lista)",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun AllAppsUsagePreview() {
    OctoOSTheme {
        AllAppsScreen(
            apps = InstalledAppsHelper.defaultCatalogApps(),
            dockApps = InstalledAppsHelper.defaultDockApps(),
            onAppClick = {},
            onBackClick = {},
            initialFilter = AllAppsFilter.USAGE
        )
    }
}

@Preview(
    name = "Busca Sem Resultados",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun AllAppsEmptyPreview() {
    OctoOSTheme {
        AllAppsScreen(
            apps = InstalledAppsHelper.defaultCatalogApps(),
            dockApps = InstalledAppsHelper.defaultDockApps(),
            onAppClick = {},
            onBackClick = {},
            initialQuery = "rua xyz"
        )
    }
}
