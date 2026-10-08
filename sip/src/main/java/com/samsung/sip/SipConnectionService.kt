package com.samsung.sip

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import android.util.Log
import com.samsung.sip.callcontroller.FakeSipCallController
import com.samsung.sip.callcontroller.Pjsua2SipCallController
import com.samsung.sip.pjsua.PjsuaManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SipConnectionService : ConnectionService() {
    private val TAG = "SipConnectionService"

    @Inject
    lateinit var pjsuaManager: PjsuaManager

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        pjsuaManager.initialize()
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d(TAG, "onCreateOutgoingConnection")
        if (request == null) {
            Log.d(TAG, "request == null, returning failed Connection")
            return Connection.createFailedConnection(DisconnectCause(DisconnectCause.ERROR))
        }
        Log.d(TAG, "request.address ${request.address}")
        val controller = Pjsua2SipCallController(pjsuaManager)
        val sipConnection = SipConnection(
            controller,
            request.address
        )
        controller.startOutgoingCall(request.address.toString())
        return sipConnection
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d(TAG, "onCreateIncomingConnection")
        if (request == null) {
            Log.d(TAG, "request == null, returning failed Connection")
            return Connection.createFailedConnection(DisconnectCause(DisconnectCause.ERROR))
        }
        val controller = Pjsua2SipCallController(pjsuaManager)
        val sipConnection = SipConnection(
            controller,
            request.address
        )

        return sipConnection
    }
}