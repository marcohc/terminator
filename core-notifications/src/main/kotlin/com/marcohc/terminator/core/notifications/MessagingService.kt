package com.marcohc.terminator.core.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class MessagingService : FirebaseMessagingService(),
    KoinComponent {

    private val notificationsRepository by inject<NotificationsRepository>()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.v("Remote message received: $remoteMessage")
        notificationsRepository.trigger(remoteMessage).blockingAwait()
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        // No-op
    }
}
