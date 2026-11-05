package com.pedro.octoos.data

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.MediaStore
import com.pedro.octoos.ui.components.OctoIconType
import java.util.Locale

object InstalledAppsHelper {

    fun defaultHomeGridApps(): List<AppEntry> = listOf(
        AppEntry(
            label = "CLAUDE",
            packageName = "com.anthropic.claude",
            monogram = "CL",
            iconType = OctoIconType.CLAUDE,
            launchCount = 3,
            usageRatio = 0.82f,
            firstInstallTime = 1730500000000L,
            recencyLabel = "SEMANA"
        ),
        AppEntry(
            label = "MAPA",
            packageName = "com.google.android.apps.maps",
            monogram = "MP",
            iconType = OctoIconType.MAPA,
            launchCount = 4,
            usageRatio = 0.56f,
            firstInstallTime = 1730400000000L,
            recencyLabel = "SEMANA"
        ),
        AppEntry(
            label = "MSGS",
            packageName = "com.google.android.apps.messaging",
            monogram = "MS",
            iconType = OctoIconType.MSGS,
            launchCount = 12,
            usageRatio = 0.72f,
            firstInstallTime = 1730300000000L,
            recencyLabel = "ONTEM"
        ),
        AppEntry(
            label = "GIT",
            packageName = "com.github.android",
            monogram = "GT",
            iconType = OctoIconType.GIT,
            launchCount = 9,
            usageRatio = 0.48f,
            firstInstallTime = 1730450000000L,
            recencyLabel = "ONTEM"
        )
    )

    fun defaultDockApps(): List<AppEntry> = listOf(
        AppEntry(
            label = "WHATSAPP",
            packageName = "com.whatsapp",
            monogram = "WA",
            iconType = OctoIconType.WHATSAPP,
            launchCount = 42,
            usageRatio = 1f,
            firstInstallTime = 1730550000000L,
            recencyLabel = "HOJE"
        ),
        AppEntry(
            label = "CÂMERA",
            packageName = "com.android.camera",
            monogram = "CM",
            iconType = OctoIconType.CAMERA,
            launchCount = 18,
            usageRatio = 0.45f,
            firstInstallTime = 1730100000000L,
            recencyLabel = "HOJE"
        ),
        AppEntry(
            label = "GIT",
            packageName = "com.github.android",
            monogram = "GT",
            iconType = OctoIconType.GIT,
            launchCount = 9,
            usageRatio = 0.35f,
            firstInstallTime = 1730450000000L,
            recencyLabel = "ONTEM"
        ),
        AppEntry(
            label = "TODOS",
            packageName = "octo.internal.all_apps",
            monogram = "TD",
            iconType = OctoIconType.TODOS
        )
    )

    fun defaultCatalogApps(): List<AppEntry> = listOf(
        AppEntry("ARQUIVOS", "com.android.documentsui", "AR", OctoIconType.CAMERA, 2, 0.25f, 1730120000000L, "SEMANA"),
        AppEntry("CÂMERA", "com.android.camera", "CM", OctoIconType.CAMERA, 18, 0.45f, 1730100000000L, "HOJE"),
        AppEntry("CLAUDE", "com.anthropic.claude", "CL", OctoIconType.CLAUDE, 3, 0.82f, 1730500000000L, "SEMANA"),
        AppEntry("COLETOR", "com.octo.collector", "CO", OctoIconType.TODOS, 2, 0.22f, 1730080000000L, "SEMANA"),
        AppEntry("CONFIG", "com.android.settings", "CF", OctoIconType.TODOS, 5, 0.40f, 1730200000000L, "ONTEM"),
        AppEntry("CALC", "com.google.android.calculator", "CA", OctoIconType.TODOS, 2, 0.18f, 1730060000000L, "SEMANA"),
        AppEntry("GIT", "com.github.android", "GT", OctoIconType.GIT, 9, 0.48f, 1730450000000L, "ONTEM"),
        AppEntry("MAPA", "com.google.android.apps.maps", "MP", OctoIconType.MAPA, 4, 0.56f, 1730400000000L, "SEMANA"),
        AppEntry("MSGS", "com.google.android.apps.messaging", "MS", OctoIconType.MSGS, 6, 0.72f, 1730300000000L, "ONTEM"),
        AppEntry("WHATSAPP", "com.whatsapp", "WA", OctoIconType.WHATSAPP, 42, 1.00f, 1730550000000L, "HOJE")
    )

    fun loadAllInstalledApps(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = runCatching { pm.queryIntentActivities(mainIntent, 0) }.getOrNull().orEmpty()
        val selfPkg = context.packageName
        val defaultsByPkg = defaultCatalogApps().associateBy { it.packageName }

        val dynamicApps = resolved.mapNotNull { info ->
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == selfPkg) return@mapNotNull null
            val rawLabel = info.loadLabel(pm)?.toString()?.trim().orEmpty()
            if (rawLabel.isEmpty()) return@mapNotNull null

            val cleanUpper = rawLabel.uppercase(Locale("pt", "BR"))
            val monogram = buildMonogram(cleanUpper)
            val installTime = runCatching {
                pm.getPackageInfo(pkg, 0).firstInstallTime
            }.getOrDefault(1730000000000L)

            val preset = defaultsByPkg[pkg]
            AppEntry(
                label = preset?.label ?: cleanUpper,
                packageName = pkg,
                monogram = preset?.monogram ?: monogram,
                iconType = preset?.iconType ?: inferIconType(pkg, cleanUpper),
                launchCount = preset?.launchCount ?: 3,
                usageRatio = preset?.usageRatio ?: 0.25f,
                firstInstallTime = installTime,
                recencyLabel = preset?.recencyLabel ?: "SEMANA"
            )
        }.distinctBy { it.packageName }

        return (defaultCatalogApps() + dynamicApps)
            .distinctBy { it.packageName }
            .sortedBy { it.label }
    }

    private fun buildMonogram(label: String): String {
        val lettersOnly = label.filter { it.isLetterOrDigit() }
        return when {
            lettersOnly.length >= 2 -> lettersOnly.substring(0, 2)
            lettersOnly.length == 1 -> "${lettersOnly}X"
            else -> "AP"
        }
    }

    private fun inferIconType(packageName: String, label: String): OctoIconType = when {
        packageName.contains("claude", true) || label.contains("CLAUDE") -> OctoIconType.CLAUDE
        packageName.contains("maps", true) || label.contains("MAPA") -> OctoIconType.MAPA
        packageName.contains("whatsapp", true) -> OctoIconType.WHATSAPP
        packageName.contains("messaging", true) || packageName.contains("mms", true) -> OctoIconType.MSGS
        packageName.contains("camera", true) || label.contains("CÂMERA") || label.contains("ARQUIVOS") -> OctoIconType.CAMERA
        packageName.contains("git", true) -> OctoIconType.GIT
        else -> OctoIconType.TODOS
    }

    fun launchGoogleMapsNavigation(context: Context, destination: String, travelMode: TravelMode) {
        val encoded = Uri.encode(destination.ifBlank { "Av. Independência" })
        val navIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("google.navigation:q=$encoded&mode=${travelMode.mapsModeCode}")
        ).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val launched = runCatching { context.startActivity(navIntent) }.isSuccess
        if (!launched) {
            val fallbackGeo = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=$encoded")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(fallbackGeo) }
        }
    }

    fun launchAppOrFallback(context: Context, entry: AppEntry) {
        val pm = context.packageManager
        val directIntent = pm.getLaunchIntentForPackage(entry.packageName)
        if (directIntent != null) {
            directIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(directIntent) }
            return
        }

        val fallbackIntent = when (entry.iconType) {
            OctoIconType.CAMERA -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            OctoIconType.MAPA -> Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=")
            }
            OctoIconType.MSGS, OctoIconType.WHATSAPP -> Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MESSAGING)
            }
            else -> null
        }

        fallbackIntent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(it) }
        }
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
