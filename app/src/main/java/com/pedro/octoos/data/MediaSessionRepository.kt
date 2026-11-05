package com.pedro.octoos.data

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import android.service.notification.NotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Mídia tocando agora (música, vídeo, podcast) vinda de qualquer app com MediaSession. */
data class MediaInfo(
    val title: String,
    val artist: String,
    val appLabel: String,
    val isPlaying: Boolean,
    val packageName: String
)

/**
 * Lê as sessões de mídia ativas do sistema (Spotify, YouTube, YouTube Music etc.).
 * Exige que o usuário ative o acesso às notificações para o OctoOS (ver OctoNotificationListener).
 */
object MediaSessionRepository {

    private val _state = MutableStateFlow<MediaInfo?>(null)
    val state: StateFlow<MediaInfo?> = _state.asStateFlow()

    private var appContext: Context? = null
    private var manager: MediaSessionManager? = null
    private var controller: MediaController? = null

    private val callback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            publish()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            publish()
        }
    }

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        bind(pickController(controllers))
    }

    fun isListenerEnabled(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            ?: return false
        return flat.split(":").any {
            ComponentName.unflattenFromString(it)?.packageName == context.packageName
        }
    }

    internal fun attach(service: NotificationListenerService) {
        appContext = service.applicationContext
        val mgr = service.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
        manager = mgr
        val component = ComponentName(service, service::class.java)
        runCatching { mgr.addOnActiveSessionsChangedListener(sessionsListener, component) }
        val sessions = runCatching { mgr.getActiveSessions(component) }.getOrNull()
        bind(pickController(sessions))
    }

    internal fun detach() {
        manager?.let { runCatching { it.removeOnActiveSessionsChangedListener(sessionsListener) } }
        controller?.unregisterCallback(callback)
        controller = null
        manager = null
        _state.value = null
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.playbackState?.state == PlaybackState.STATE_PLAYING) {
            c.transportControls?.pause()
        } else {
            c.transportControls?.play()
        }
    }

    fun next() {
        controller?.transportControls?.skipToNext()
    }

    fun previous() {
        controller?.transportControls?.skipToPrevious()
    }

    private fun pickController(list: List<MediaController>?): MediaController? {
        val sessions = list.orEmpty()
        return sessions.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: sessions.firstOrNull()
    }

    private fun bind(next: MediaController?) {
        if (next?.sessionToken != controller?.sessionToken) {
            controller?.unregisterCallback(callback)
            controller = next
            next?.registerCallback(callback)
        }
        publish()
    }

    private fun publish() {
        val c = controller
        if (c == null) {
            _state.value = null
            return
        }
        val meta = c.metadata
        val title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty()
        val artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: ""
        val playing = c.playbackState?.state == PlaybackState.STATE_PLAYING
        if (title.isBlank() && !playing) {
            _state.value = null
            return
        }
        val pkg = c.packageName.orEmpty()
        val label = appContext?.packageManager?.let { pm ->
            runCatching { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() }.getOrNull()
        } ?: pkg
        _state.value = MediaInfo(
            title = title,
            artist = artist,
            appLabel = label.uppercase(),
            isPlaying = playing,
            packageName = pkg
        )
    }
}
