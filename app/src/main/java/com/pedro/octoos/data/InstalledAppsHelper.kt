package com.pedro.octoos.data

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.MediaStore
import com.pedro.octoos.ui.components.OctoIconType

object InstalledAppsHelper {

    fun defaultHomeGridApps(): List<AppEntry> = listOf(
        AppEntry(
            label = "CLAUDE",
            packageName = "com.anthropic.claude",
            monogram = "CL",
            iconType = OctoIconType.CLAUDE,
            launchCount = 48,
            usageRatio = 0.86f
        ),
        AppEntry(
            label = "MAPA",
            packageName = "com.google.android.apps.maps",
            monogram = "MP",
            iconType = OctoIconType.MAPA,
            launchCount = 31,
            usageRatio = 0.56f
        ),
        AppEntry(
            label = "MSGS",
            packageName = "com.google.android.apps.messaging",
            monogram = "MS",
            iconType = OctoIconType.MSGS,
            launchCount = 40,
            usageRatio = 0.72f
        ),
        AppEntry(
            label = "GIT",
            packageName = "com.github.android",
            monogram = "GT",
            iconType = OctoIconType.GIT,
            launchCount = 26,
            usageRatio = 0.48f
        )
    )

    fun defaultDockApps(): List<AppEntry> = listOf(
        AppEntry(
            label = "WHATSAPP",
            packageName = "com.whatsapp",
            monogram = "WA",
            iconType = OctoIconType.WHATSAPP
        ),
        AppEntry(
            label = "CÂMERA",
            packageName = "com.android.camera",
            monogram = "CM",
            iconType = OctoIconType.CAMERA
        ),
        AppEntry(
            label = "GIT",
            packageName = "com.github.android",
            monogram = "GT",
            iconType = OctoIconType.GIT
        ),
        AppEntry(
            label = "TODOS",
            packageName = "octo.internal.all_apps",
            monogram = "TD",
            iconType = OctoIconType.TODOS
        )
    )

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

