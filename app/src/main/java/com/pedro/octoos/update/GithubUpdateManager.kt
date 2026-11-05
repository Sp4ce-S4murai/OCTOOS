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
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateReleaseInfo(
    val currentVersionLabel: String = BuildConfig.VERSION_NAME,
    val targetVersionLabel: String = "",
    val remoteVersionCode: Int = 0,
    val changelogItems: List<String> = emptyList(),
    val apkDownloadUrl: String? = null
)

sealed class UpdateCheckResult {
    data class Available(val info: UpdateReleaseInfo) : UpdateCheckResult()
    object UpToDate : UpdateCheckResult()
    object Failed : UpdateCheckResult()
}

object GithubUpdateManager {

    /** Consulta a última Release pública. O versionCode vem do número no final da tag (vX.Y.N). */
    suspend fun checkLatestRelease(): UpdateCheckResult = withContext(Dispatchers.IO) {
        val remote = runCatching { fetchFromGithubApi() }.getOrNull()
            ?: return@withContext UpdateCheckResult.Failed
        if (remote.remoteVersionCode > BuildConfig.VERSION_CODE) {
            UpdateCheckResult.Available(remote)
        } else {
            UpdateCheckResult.UpToDate
        }
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
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                null
            } else {
                val payload = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(payload)
                val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                val body = json.optString("body", "").trim()

                val parsedCode = tagName.split(".").lastOrNull()
                    ?.filter { it.isDigit() }
                    ?.toIntOrNull() ?: 0

                var apkUrl: String? = null
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i) ?: continue
                        val name = asset.optString("name", "")
                        val url = asset.optString("browser_download_url", "")
                        if (name.endsWith(".apk", ignoreCase = true) && url.isNotEmpty()) {
                            apkUrl = url
                            break
                        }
                    }
                }

                val bullets = body.lines()
                    .map { it.trim().removePrefix("-").removePrefix("*").removePrefix(">").trim() }
                    .filter { it.isNotEmpty() }
                    .take(5)

                UpdateReleaseInfo(
                    currentVersionLabel = BuildConfig.VERSION_NAME,
                    targetVersionLabel = tagName,
                    remoteVersionCode = parsedCode,
                    changelogItems = bullets.ifEmpty { listOf("Sem notas de versão.") },
                    apkDownloadUrl = apkUrl
                )
            }
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

    /** Baixa o APK da Release com progresso real e instala pelo PackageInstaller. */
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

        val apkUrl = releaseInfo.apkDownloadUrl ?: return@withContext false

        val targetFile = File(context.cacheDir, "octoos-update.apk")
        val downloaded = runCatching {
            val conn = (URL(apkUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 15000
                instanceFollowRedirects = true
            }
            conn.connect()
            if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}")
            // -1 quando o servidor não informa o tamanho: a barra fica indeterminada.
            val totalBytes = conn.getHeaderFieldLong("Content-Length", -1L)
            var bytesReadTotal = 0L
            withContext(Dispatchers.Main) { onProgressPercent(0) }

            conn.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesReadTotal += read
                        if (totalBytes > 0L) {
                            val pct = ((bytesReadTotal * 100L) / totalBytes).toInt().coerceIn(0, 100)
                            withContext(Dispatchers.Main) { onProgressPercent(pct) }
                        }
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
