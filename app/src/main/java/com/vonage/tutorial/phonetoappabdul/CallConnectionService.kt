package com.vonage.tutorial.phonetoappabdul

import android.net.Uri
import android.telecom.*


class CallConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?): Connection {
        /*
        CSDemo: (5) This gets the from number from the call invite in the ClientManager.
        A CallConnection Object is also created. This is how the system tells you the user has
        initiated an action such and answering/rejecting the call with the System UI.
         */
        val from = request?.extras?.getString("from")
        val connection = CallConnection(this)
        connection.setAddress(Uri.parse("tel:$from"), TelecomManager.PRESENTATION_ALLOWED)
        connection.connectionProperties = Connection.PROPERTY_SELF_MANAGED
        connection.setInitializing()
        return connection
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
    }
}