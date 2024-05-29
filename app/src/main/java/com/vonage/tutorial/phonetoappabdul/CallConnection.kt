package com.vonage.tutorial.phonetoappabdul

import android.content.Context
import android.telecom.Connection

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