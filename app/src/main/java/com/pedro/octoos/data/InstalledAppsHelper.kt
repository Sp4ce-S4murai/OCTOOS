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

    const val ALL_APPS_PACKAGE = "octo.internal.all_apps"
    const val SETTINGS_PACKAGE = "octo.internal.settings"

    /** Apps fixos da grade inicial. Só aparecem se o app estiver instalado (ver launchAppOrFallback). */
    fun defaultHomeGridApps(): List<AppEntry> = listOf(
        AppEntry(label = "CLAUDE", packageName = "com.anthropic.claude", monogram = "CL", iconType = OctoIconType.CLAUDE),
        AppEntry(label = "MAPA", packageName = "com.google.android.apps.maps", monogram = "MP", iconType = OctoIconType.MAPA),
        AppEntry(label = "MSGS", packageName = "com.google.android.apps.messaging", monogram = "MS", iconType = OctoIconType.MSGS),
        AppEntry(label = "GIT", packageName = "com.github.android", monogram = "GT", iconType = OctoIconType.GIT)
    )

    fun defaultDockApps(): List<AppEntry> = listOf(
        AppEntry(label = "WHATSAPP", packageName = "com.whatsapp", monogram = "WA", iconType = OctoIconType.WHATSAPP),
        AppEntry(label = "CÂMERA", packageName = "com.android.camera", monogram = "CM", iconType = OctoIconType.CAMERA),
        AppEntry(label = "GIT", packageName = "com.github.android", monogram = "GT", iconType = OctoIconType.GIT),
        AppEntry(label = "TODOS", packageName = ALL_APPS_PACKAGE, monogram = "TD", iconType = OctoIconType.TODOS)
    )

    /** Todos os apps com ícone de launcher, mais as entradas internas CONFIG. */
    fun loadAllInstalledApps(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = runCatching { pm.queryIntentActivities(mainIntent, 0) }.getOrNull().orEmpty()
        val selfPkg = context.packageName

        val installed = resolved.mapNotNull { info ->
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == selfPkg) return@mapNotNull null
            val rawLabel = info.loadLabel(pm)?.toString()?.trim().orEmpty()
            if (rawLabel.isEmpty()) return@mapNotNull null

            val cleanUpper = rawLabel.uppercase(Locale("pt", "BR"))
            val installTime = runCatching {
                pm.getPackageInfo(pkg, 0).firstInstallTime
            }.getOrDefault(0L)

            AppEntry(
                label = cleanUpper,
                packageName = pkg,
                monogram = buildMonogram(cleanUpper),
                iconType = inferIconType(pkg, cleanUpper),
                firstInstallTime = installTime
            )
        }.distinctBy { it.packageName }

        val internal = listOf(
            AppEntry(label = "CONFIG", packageName = SETTINGS_PACKAGE, monogram = "CF", iconType = OctoIconType.SETTINGS)
        )

        return (installed + internal)
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

    private data class IconRule(
        val type: OctoIconType,
        val packages: List<String> = emptyList(),
        val labels: List<String> = emptyList()
    )

    /** Ordem importa: regras mais específicas (YouTube Music) vêm antes das genéricas. */
    private val ICON_RULES = listOf(
        IconRule(OctoIconType.CLAUDE, listOf("com.anthropic.claude"), listOf("CLAUDE")),
        IconRule(OctoIconType.MAPA, listOf("com.google.android.apps.maps"), listOf("MAPA")),
        IconRule(OctoIconType.WHATSAPP, listOf("com.whatsapp"), listOf("WHATSAPP")),
        IconRule(
            OctoIconType.MSGS,
            listOf("com.google.android.apps.messaging", "com.samsung.android.messaging", "com.android.mms"),
            listOf("MENSAGENS", "MESSAGES")
        ),
        IconRule(
            OctoIconType.CAMERA,
            listOf("com.android.camera", "com.google.android.googlecamera", "com.sec.android.app.camera"),
            listOf("CÂMERA", "CAMERA")
        ),
        IconRule(OctoIconType.GIT, listOf("com.github.android"), listOf("GITHUB")),
        IconRule(OctoIconType.MUSIC, listOf("com.google.android.apps.youtube.music", "com.google.android.music"), listOf("YOUTUBE MUSIC")),
        IconRule(OctoIconType.YOUTUBE, listOf("com.google.android.youtube"), listOf("YOUTUBE")),
        IconRule(OctoIconType.SPOTIFY, listOf("com.spotify.music"), listOf("SPOTIFY")),
        IconRule(OctoIconType.INSTAGRAM, listOf("com.instagram.android"), listOf("INSTAGRAM")),
        IconRule(OctoIconType.GMAIL, listOf("com.google.android.gm"), listOf("GMAIL")),
        IconRule(OctoIconType.CHROME, listOf("com.android.chrome", "org.mozilla.firefox"), listOf("CHROME")),
        IconRule(
            OctoIconType.PHOTOS,
            listOf("com.google.android.apps.photos", "com.sec.android.gallery3d", "com.android.gallery3d"),
            listOf("FOTOS", "GALERIA", "PHOTOS", "GALLERY")
        ),
        IconRule(
            OctoIconType.CALENDAR,
            listOf("com.google.android.calendar", "com.samsung.android.calendar"),
            listOf("AGENDA", "CALENDÁRIO", "CALENDAR")
        ),
        IconRule(
            OctoIconType.NOTES,
            listOf("com.google.android.keep", "com.samsung.android.app.notes", "com.miui.notes"),
            listOf("NOTAS", "NOTES")
        ),
        IconRule(OctoIconType.TELEGRAM, listOf("org.telegram.messenger"), listOf("TELEGRAM")),
        IconRule(OctoIconType.X, listOf("com.twitter.android", "com.x.android")),
        IconRule(
            OctoIconType.PHONE,
            listOf("com.google.android.dialer", "com.samsung.android.dialer", "com.android.dialer"),
            listOf("TELEFONE", "PHONE")
        ),
        IconRule(OctoIconType.SETTINGS, listOf("com.android.settings", SETTINGS_PACKAGE), listOf("CONFIGURA"))
    )

    /**
     * Detecta o ícone temático pelo pacote (exato/contém) e, se não bater, pelo rótulo.
     * Retorna null para apps desconhecidos: a grade mostra o monograma em vez de um ícone genérico.
     */
    fun inferIconType(packageName: String, label: String): OctoIconType? {
        val pkg = packageName.lowercase(Locale.ROOT)
        val lbl = label.uppercase(Locale("pt", "BR"))
        return ICON_RULES.firstOrNull { rule ->
            rule.packages.any { pkg.contains(it.lowercase(Locale.ROOT)) } ||
                rule.labels.any { lbl.contains(it) }
        }?.type
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

    /** Abre o app. Se não estiver instalado, tenta um app equivalente pelo tipo. */
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
            ?: return false
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
