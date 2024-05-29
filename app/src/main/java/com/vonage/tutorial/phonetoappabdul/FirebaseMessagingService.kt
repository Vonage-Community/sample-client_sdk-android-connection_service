package com.vonage.tutorial.phonetoappabdul

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService: FirebaseMessagingService() {

    companion object {
        fun requestToken(onSuccessCallback: ((String) -> Unit)? = null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { token ->
                        onSuccessCallback?.invoke(token)
                    }
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        ClientManager.getInstance(applicationContext).registerToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // CSDemo: (1) When an incoming push notification comes in, notify the ClientManager
        ClientManager.getInstance(applicationContext).startIncomingCall(remoteMessage)
    }
}