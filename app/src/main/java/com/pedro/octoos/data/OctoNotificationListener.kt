package com.pedro.octoos.data

import android.service.notification.NotificationListenerService

/**
 * Serviço vazio usado só para o Android liberar o acesso às sessões de mídia.
 * O usuário ativa em Configurações > Acesso às notificações > OctoOS.
 */
class OctoNotificationListener : NotificationListenerService() {
    override fun onListenerConnected() {
        MediaSessionRepository.attach(this)
    }

    override fun onListenerDisconnected() {
        MediaSessionRepository.detach()
    }
}
