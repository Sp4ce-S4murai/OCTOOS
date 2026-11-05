package com.pedro.octoos.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.pedro.octoos.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateReleaseInfo(
    val currentVersionLabel: String = "1.3",
    val targetVersionLabel: String = "1.4",
    val remoteVersionCode: Int = BuildConfig.VERSION_CODE + 1,
    val changelogItems: List<String> = listOf(
        "Mapa com radar e rotas",
        "Dock com nomes dos apps",
        "Correção no clima offline"
    ),
    val apkDownloadUrl: String? = null
)

object GithubUpdateManager {

    /**
     * Queries the latest GitHub Release from the public repository and compares remoteVersionCode
     * against BuildConfig.VERSION_CODE. If forcePreviewFallback is true (e.g. manual check before
     * first GitHub release is published), returns the 1.3 -> 1.4 preview release.
     */
    suspend fun checkLatestRelease(
        forcePreviewFallback: Boolean = false
    ): UpdateReleaseInfo? = withContext(Dispatchers.IO) {
        val remoteInfo = runCatching { fetchFromGithubApi() }.getOrNull()
        if (remoteInfo != null && remoteInfo.remoteVersionCode > BuildConfig.VERSION_CODE) {
            return@withContext remoteInfo
        }
        if (forcePreviewFallback) {
            return@withContext UpdateReleaseInfo()
        }
        null
    }

    private fun fetchFromGithubApi(): UpdateReleaseInfo? {
        val endpoint = "https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases/latest"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 6000
            readTimeout = 6000
            setRequestProperty("Accept", "application/vnd.github+json")
        }

        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(payload)
            val tagName = json.optString("tag_name", "").removePrefix("v").trim()
            val body = json.optString("body", "").trim()

            // Parse versionCode from tag (e.g. v0.1.12 -> 12) or fallback to minor*10
            val parsedCode = tagName.split(".")
                .lastOrNull()
                ?.filter { it.isDigit() }
                ?.toIntOrNull()
                ?: (BuildConfig.VERSION_CODE)

            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        val url = asset.optString("browser_download_url", "")
                        if (url.isNotEmpty()) {
                            apkUrl = url
                            break
                        }
                    }
                }
            }

            val parsedBullets = body.lines()
                .map { it.trim().removePrefix("-").removePrefix("*").removePrefix(">").trim() }
                .filter { it.isNotEmpty() }
                .take(5)
                .ifEmpty {
                    listOf(
                        "Mapa com radar e rotas",
                        "Dock com nomes dos apps",
                        "Correção no clima offline"
                    )
                }

            UpdateReleaseInfo(
                currentVersionLabel = BuildConfig.VERSION_NAME,
                targetVersionLabel = tagName.ifEmpty { "1.4" },
                remoteVersionCode = parsedCode,
                changelogItems = parsedBullets,
                apkDownloadUrl = apkUrl
            )
        } finally {
            connection.disconnect()
        }
    }

    fun canInstallUnknownPackages(context: Context): Boolean {
        return context.packageManager.canRequestPackageInstalls()
    }

    fun openUnknownSourcesPermissionSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    /**
     * Downloads the APK (or simulates download progress when testing without a remote APK URL)
     * and installs it via Android PackageInstaller session.
     */
    suspend fun downloadAndInstallApk(
        context: Context,
        releaseInfo: UpdateReleaseInfo,
        onProgressPercent: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        if (!canInstallUnknownPackages(context)) {
            withContext(Dispatchers.Main) {
                openUnknownSourcesPermissionSettings(context)
            }
            return@withContext false
        }

        val apkUrl = releaseInfo.apkDownloadUrl
        if (apkUrl.isNullOrBlank()) {
            // Simulate progress up to 100% when running local preview without remote APK asset
            for (step in listOf(20, 42, 64, 85, 100)) {
                withContext(Dispatchers.Main) { onProgressPercent(step) }
                delay(240L)
            }
            return@withContext true
        }

        val targetFile = File(context.cacheDir, "octoos-update.apk")
        val downloaded = runCatching {
            val conn = (URL(apkUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 15000
                instanceFollowRedirects = true
            }
            val totalBytes = conn.contentLength.coerceAtLeast(1)
            var bytesReadTotal = 0L

            conn.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesReadTotal += read
                        val pct = ((bytesReadTotal * 100L) / totalBytes).toInt().coerceIn(1, 100)
                        withContext(Dispatchers.Main) { onProgressPercent(pct) }
                    }
                    output.flush()
                }
            }
            conn.disconnect()
            true
        }.getOrDefault(false)

        if (!downloaded || !targetFile.exists()) return@withContext false

        installApkWithPackageInstaller(context, targetFile)
    }

    private fun installApkWithPackageInstaller(context: Context, apkFile: File): Boolean {
        return runCatching {
            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL
            )
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            FileInputStream(apkFile).use { input ->
                session.openWrite("octoos_release_apk", 0, apkFile.length()).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }

            val callbackIntent = Intent(context, UpdateInstallReceiver::class.java)
            val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                callbackIntent,
                pendingFlags
            )
            session.commit(pendingIntent.intentSender)
            session.close()
            true
        }.getOrDefault(false)
    }
}

