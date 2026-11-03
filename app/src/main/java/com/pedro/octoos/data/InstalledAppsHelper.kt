package com.pedro.octoos.data

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
            launchCount = 48,
            usageRatio = 0.86f,
            firstInstallTime = 1730500000000L
        ),
        AppEntry(
            label = "MAPA",
            packageName = "com.google.android.apps.maps",
            monogram = "MP",
            iconType = OctoIconType.MAPA,
            launchCount = 31,
            usageRatio = 0.56f,
            firstInstallTime = 1730400000000L
        ),
        AppEntry(
            label = "MSGS",
            packageName = "com.google.android.apps.messaging",
            monogram = "MS",
            iconType = OctoIconType.MSGS,
            launchCount = 40,
            usageRatio = 0.72f,
            firstInstallTime = 1730300000000L
        ),
        AppEntry(
            label = "GIT",
            packageName = "com.github.android",
            monogram = "GT",
            iconType = OctoIconType.GIT,
            launchCount = 26,
            usageRatio = 0.48f,
            firstInstallTime = 1730450000000L
        )
    )

    fun defaultDockApps(): List<AppEntry> = listOf(
        AppEntry(
            label = "WHATSAPP",
            packageName = "com.whatsapp",
            monogram = "WA",
            iconType = OctoIconType.WHATSAPP,
            launchCount = 64,
            usageRatio = 1f,
            firstInstallTime = 1730550000000L
        ),
        AppEntry(
            label = "CÂMERA",
            packageName = "com.android.camera",
            monogram = "CM",
            iconType = OctoIconType.CAMERA,
            launchCount = 19,
            usageRatio = 0.34f,
            firstInstallTime = 1730100000000L
        ),
        AppEntry(
            label = "GIT",
            packageName = "com.github.android",
            monogram = "GT",
            iconType = OctoIconType.GIT,
            launchCount = 26,
            usageRatio = 0.48f,
            firstInstallTime = 1730450000000L
        ),
        AppEntry(
            label = "TODOS",
            packageName = "octo.internal.all_apps",
            monogram = "TD",
            iconType = OctoIconType.TODOS
        )
    )

    fun defaultCatalogApps(): List<AppEntry> = listOf(
        AppEntry("ARQUIVOS", "com.android.documentsui", "AR", null, 14, 0.25f, 1730120000000L),
        AppEntry("CALCULADORA", "com.google.android.calculator", "CA", null, 9, 0.18f, 1730080000000L),
        AppEntry("CÂMERA", "com.android.camera", "CM", OctoIconType.CAMERA, 19, 0.34f, 1730100000000L),
        AppEntry("CLAUDE", "com.anthropic.claude", "CL", OctoIconType.CLAUDE, 48, 0.86f, 1730500000000L),
        AppEntry("CONFIG", "com.android.settings", "CF", null, 22, 0.40f, 1730200000000L),
        AppEntry("GALERIA", "com.google.android.apps.photos", "GL", null, 17, 0.30f, 1730250000000L),
        AppEntry("GIT", "com.github.android", "GT", OctoIconType.GIT, 26, 0.48f, 1730450000000L),
        AppEntry("MAPA", "com.google.android.apps.maps", "MP", OctoIconType.MAPA, 31, 0.56f, 1730400000000L),
        AppEntry("MSGS", "com.google.android.apps.messaging", "MS", OctoIconType.MSGS, 40, 0.72f, 1730300000000L),
        AppEntry("MÚSICA", "com.spotify.music", "MU", null, 33, 0.60f, 1730480000000L),
        AppEntry("NAVEGADOR", "com.android.chrome", "NV", null, 52, 0.90f, 1730350000000L),
        AppEntry("NOTAS", "com.standardnotes", "NT", null, 15, 0.28f, 1730520000000L),
        AppEntry("RELÓGIO", "com.google.android.deskclock", "RL", null, 11, 0.22f, 1730150000000L),
        AppEntry("TERMINAL", "com.termux", "TM", null, 37, 0.66f, 1730540000000L),
        AppEntry("WHATSAPP", "com.whatsapp", "WA", OctoIconType.WHATSAPP, 64, 1.00f, 1730550000000L)
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
                launchCount = preset?.launchCount ?: 8,
                usageRatio = preset?.usageRatio ?: 0.25f,
                firstInstallTime = installTime
            )
        }.distinctBy { it.packageName }

        val combined = (defaultCatalogApps() + dynamicApps)
            .distinctBy { it.packageName }
            .sortedBy { it.label }

        return combined
    }

    private fun buildMonogram(label: String): String {
        val lettersOnly = label.filter { it.isLetterOrDigit() }
        return when {
            lettersOnly.length >= 2 -> lettersOnly.substring(0, 2)
            lettersOnly.length == 1 -> "${lettersOnly}X"
            else -> "AP"
        }
    }

    private fun inferIconType(packageName: String, label: String): OctoIconType? = when {
        packageName.contains("claude", true) || label.contains("CLAUDE") -> OctoIconType.CLAUDE
        packageName.contains("maps", true) || label.contains("MAPA") -> OctoIconType.MAPA
        packageName.contains("whatsapp", true) -> OctoIconType.WHATSAPP
        packageName.contains("messaging", true) || packageName.contains("mms", true) -> OctoIconType.MSGS
        packageName.contains("camera", true) || label.contains("CÂMERA") -> OctoIconType.CAMERA
        packageName.contains("git", true) -> OctoIconType.GIT
        else -> null
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
                data = android.net.Uri.parse("geo:0,0?q=")
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
