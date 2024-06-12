package com.vonage.tutorial.phonetoappabdul

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import com.google.firebase.messaging.RemoteMessage
import com.vonage.voice.api.*
import android.Manifest
import android.telecom.DisconnectCause
import androidx.core.content.edit
import com.vonage.android_core.PushType
import com.vonage.android_core.VGClientConfig
import com.vonage.clientcore.core.api.ClientConfigRegion

class ClientManager(private val context: Context) {
    private var callInvite: CallId? = null
    private var session: String? = null
    private var call: CallId? = null

    private var token = "ALICE_JWT"

    private lateinit var client: VoiceClient
    private lateinit var telecomManager: TelecomManager
    private lateinit var phoneAccountHandle: PhoneAccountHandle

    companion object {
        // Volatile will guarantee a thread-safe & up-to-date version of the instance
        @Volatile
        private var instance: ClientManager? = null

        fun getInstance(context: Context): ClientManager {
            return instance ?: synchronized(this) {
                instance ?: ClientManager(context).also { instance = it }
            }
        }
    }

    init {
        val config = VGClientConfig(ClientConfigRegion.US)
        client = VoiceClient(context)
        client.setConfig(config)
        setListeners()

        val componentName = ComponentName(context, CallConnectionService::class.java)
        telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        phoneAccountHandle = PhoneAccountHandle(componentName, "Vonage Voip Calling")
        val phoneAccount = PhoneAccount.builder(phoneAccountHandle, "Vonage Voip Calling")
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER).build()
        telecomManager.registerPhoneAccount(phoneAccount)
    }

    fun login(callback: ((String) -> Unit)? = null) {
        client.createSession(token) { err, sessionId ->
            when {
                err != null -> {
                    callback?.invoke(err.localizedMessage)
                }

                else -> {
                    session = sessionId
                    callback?.invoke("Connected")
                }
            }
        }
    }

    private fun processPushCallInvite(remoteMessage: RemoteMessage) {
        // CSDemo: (3) Give the incoming push to the SDK to process
        client.processPushCallInvite(remoteMessage.data.toString())
    }

    private fun setListeners() {
        // CSDemo: (4) If the push is processed correctly, the setCallInviteListener is called.
        client.setCallInviteListener { incomingCallId, from, _ ->
            if (telecomManager.isIncomingCallPermitted(phoneAccountHandle)) {
                callInvite = incomingCallId
                val extras = Bundle()
                extras.putString("from", from)

                // CSDemo: This calls the onCreateIncomingConnection function in the CallConnectionService class
                telecomManager.addNewIncomingCall(phoneAccountHandle, extras)
            }
        }
    }

    fun getStoredToken(): String? {
        return context.getSharedPreferences("vonage_pref", Context.MODE_PRIVATE).getString("token", null)
    }

    fun registerToken(token: String) {
        // CSDemo: (0) For push to work, you need to register a token. This token maps this device to this user.
        client.registerDevicePushToken(token) { err, deviceId ->
            if (err != null) {
                println("there was an error: $err")
            } else {
                context.getSharedPreferences("vonage_pref", Context.MODE_PRIVATE).edit {
                    putString("token", token)
                    putString("deviceId", deviceId)
                }
                println("registered device push token successfully - device id: $deviceId")
            }
        }
    }

    fun startIncomingCall(remoteMessage: RemoteMessage) {
        /*
        CSDemo: (2) To process and answer a call, your user needs to be logged in.
        When the user is logged in, you can process the push. If the push processes
        correctly you will get a callInvite. This also checks for the correct push type
        and if the app has permission to use the system UI for calling.
        */

        if (context.checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS) == PackageManager.PERMISSION_GRANTED) {
            val type: PushType = VoiceClient.getPushNotificationType(remoteMessage.data.toString())

            when (type) {
                PushType.INCOMING_CALL -> {
                    login { status ->
                        if (status == "Connected") {
                            processPushCallInvite(remoteMessage)
                        }
                    }
                }

                else -> {}
            }
        }
    }

    fun answerCall(callConnection: CallConnection) {
        // CSDemo: (7) The answer button has been pressed in the system UI. Answer the call with the SDK
        callInvite?.let {
            client.answer(it) { err ->
                when {
                    err != null -> {
                        print("error answering call")
                    }

                    else -> {
                        call = it
                        callConnection.setActive()
                        print("call answered")
                    }
                }
            }
        }
    }

    fun rejectCall(callConnection: CallConnection) {
        callInvite?.let {
            client.reject(it) { err ->
                when {
                    err != null -> {
                        print("error rejecting call")
                    }

                    else -> {
                        callConnection.setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
                        callConnection.destroy()
                        print("call rejected")
                    }
                }
            }
        }
    }

    fun endCall(callConnection: CallConnection) {
        call?.let {
            client.hangup(it) { err ->
                when {
                    err != null -> {
                        print("error rejecting call")
                    }

                    else -> {
                        print("call rejected")
                        callConnection.setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
                        callConnection.destroy()
                    }
                }
            }
        }
    }
}