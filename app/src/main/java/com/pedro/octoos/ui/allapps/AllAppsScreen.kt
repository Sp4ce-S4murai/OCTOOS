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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.data.AppEntry
import com.pedro.octoos.data.InstalledAppsHelper
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
import com.pedro.octoos.ui.theme.OctoTile
import com.pedro.octoos.ui.theme.OrbitronFamily
import com.pedro.octoos.ui.theme.ShareTechMonoFamily
import java.text.Normalizer
import java.util.Locale

enum class AllAppsFilter(val label: String) {
    AZ("A–Z"),
    USAGE("USO"),
    NEW("NOVOS")
}

@Composable
fun AllAppsScreen(
    apps: List<AppEntry>,
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
        AllAppsHeader(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(14.dp))

        SearchInputBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onClear = { searchQuery = "" }
        )

        Spacer(modifier = Modifier.height(12.dp))

        FilterChipsRow(
            selected = selectedFilter,
            onSelect = { selectedFilter = it },
            totalCount = filteredApps.size
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
        }
    }
}

@Composable
private fun AllAppsHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TODOS OS APPS",
            fontFamily = OrbitronFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = OctoText,
            letterSpacing = 1.5.sp
        )
        Box(
            modifier = Modifier
                .clip(ChamferedShape(8.dp))
                .background(OctoTile)
                .border(1.dp, OctoBorder, ChamferedShape(8.dp))
                .clickable(onClick = onBackClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "← INÍCIO",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime
            )
        }
    }
}

@Composable
private fun SearchInputBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    val shape = ChamferedShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .background(OctoTile)
            .border(
                width = 1.dp,
                color = if (query.isNotEmpty()) OctoLime else OctoBorder,
                shape = shape
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ">",
            fontFamily = ShareTechMonoFamily,
            fontSize = 16.sp,
            color = OctoLime
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = "BUSCAR APLICATIVO...",
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 12.sp,
                    color = OctoGray,
                    letterSpacing = 0.8.sp
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
                    color = OctoText,
                    letterSpacing = 0.8.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            Text(
                text = "LIMPAR",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoLime,
                modifier = Modifier
                    .clickable(onClick = onClear)
                    .padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun FilterChipsRow(
    selected: AllAppsFilter,
    onSelect: (AllAppsFilter) -> Unit,
    totalCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AllAppsFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            val chipShape = ChamferedShape(10.dp)
            Box(
                modifier = Modifier
                    .clip(chipShape)
                    .background(if (isSelected) OctoLime else OctoTile)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) OctoLime else OctoBorder,
                        shape = chipShape
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.label,
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = if (isSelected) OctoBackground else OctoText,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "$totalCount APPS",
            fontFamily = ShareTechMonoFamily,
            fontSize = 12.sp,
            color = OctoGray
        )
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
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        grouped.forEach { (letter, sectionApps) ->
            item(key = "header_$letter") {
                LetterSeparatorHeader(letter = letter)
            }

            val rows = sectionApps.chunked(4)
            items(
                items = rows,
                key = { row -> "row_${letter}_${row.first().packageName}" }
            ) { rowApps ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0 until 4) {
                        val app = rowApps.getOrNull(col)
                        if (app != null) {
                            AllAppsGridTile(
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
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = letter,
            fontFamily = OrbitronFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
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
private fun AllAppsGridTile(
    entry: AppEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = ChamferedShape(12.dp)
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(shape)
            .background(OctoTile)
            .border(1.dp, OctoBorder, shape)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (entry.iconType != null) {
                OctoSolidIcon(
                    type = entry.iconType,
                    size = 19.dp,
                    tint = OctoLime
                )
            } else {
                Text(
                    text = entry.monogram,
                    fontFamily = BebasNeueFamily,
                    fontSize = 20.sp,
                    color = OctoLime,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = entry.label,
                fontFamily = IbmPlexMonoFamily,
                fontSize = 9.sp,
                color = OctoText,
                maxLines = 1
            )
        }

        // 3.dp usage bar at the bottom of the tile
        val ratio = entry.usageRatio.coerceIn(0.12f, 1f)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(ratio)
                .height(3.dp)
                .background(OctoLime)
        )
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
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
    val shape = ChamferedShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(OctoTile)
            .border(1.dp, OctoBorder, shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (entry.iconType != null) {
                    OctoSolidIcon(
                        type = entry.iconType,
                        size = 20.dp,
                        tint = OctoLime
                    )
                } else {
                    Text(
                        text = entry.monogram,
                        fontFamily = BebasNeueFamily,
                        fontSize = 20.sp,
                        color = OctoLime
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = entry.label,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = OctoText,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            // Open count on the right
            Text(
                text = "${entry.launchCount}",
                fontFamily = ShareTechMonoFamily,
                fontSize = 16.sp,
                color = OctoLime
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "ABERTURAS",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 9.sp,
                color = OctoGray
            )
        }

        // 3.dp usage bar at the bottom of the list tile
        val ratio = entry.usageRatio.coerceIn(0.10f, 1f)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(ratio)
                .height(3.dp)
                .background(OctoLime)
        )
    }
}

@Composable
private fun EmptyResultsState(modifier: Modifier = Modifier) {
    val shape = ChamferedShape(12.dp)
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(OctoTile)
                .border(1.dp, OctoBorder, shape)
                .padding(vertical = 36.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SEM RESULTADOS",
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = OctoLime,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "NENHUM APLICATIVO CORRESPONDE À BUSCA",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoGray,
                letterSpacing = 0.8.sp
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
    name = "Todos os Apps (A–Z Grade)",
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
            onAppClick = {},
            onBackClick = {},
            initialFilter = AllAppsFilter.AZ
        )
    }
}

@Preview(
    name = "Todos os Apps (USO Lista)",
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
            onAppClick = {},
            onBackClick = {},
            initialFilter = AllAppsFilter.USAGE
        )
    }
}

@Preview(
    name = "Todos os Apps (SEM RESULTADOS)",
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
            onAppClick = {},
            onBackClick = {},
            initialQuery = "XYZ_INEXISTENTE"
        )
    }
}

