package com.samsung.sip

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.util.Log

class SipConnectionService : ConnectionService() {
    private val TAG = "SipConnectionService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        // TODO: Init PJSUA2 manager
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val sipConnection = SipConnection(
            context = this,
            destination = request?.address
        )
        sipConnection.startFakeOutgoingCall()
        Log.d(TAG, "onCreateOutgoingConnection")
        return sipConnection
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val sipConnection = SipConnection(
            context = this,
            destination = request?.address
        )

        sipConnection.startFakeIncomingCall()
        Log.d(TAG, "onCreateIncomingConnection")
        return sipConnection
    }
}