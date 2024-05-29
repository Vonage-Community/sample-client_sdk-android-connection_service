package com.vonage.tutorial.phonetoappabdul24

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.widget.Button
import com.google.firebase.messaging.RemoteMessage
import com.vonage.android_core.PushType
import com.vonage.voice.api.CallId
import com.vonage.voice.api.VoiceClient

class CallConnection(
    context: Context
) : Connection() {

    private var clientManager = ClientManager.getInstance(context)

    // CSDemo: (6) Here you will get events back from the system UI.

    override fun onDisconnect() {
        clientManager.endCall(this)
    }

    override fun onAnswer() {
        clientManager.answerCall(this)
    }

    override fun onReject() {
        clientManager.rejectCall(this)
    }
}