package com.samsung.sip

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import android.util.Log
import com.samsung.sip.callcontroller.FakeSipCallController
import com.samsung.sip.callcontroller.Pjsua2SipCallController
import com.samsung.sip.callcontroller.SipIncomingCallCoordinator.Companion.STRING_KEY_PJSUA_CALL_ID
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
        Log.d(TAG, "phoneAccount ${connectionManagerPhoneAccount}")
        Log.d(TAG, "request.address ${request.address}")
        val controller = Pjsua2SipCallController(pjsuaManager)
        val sipConnection = SipConnection(
            controller,
            request.address
        )
        sipConnection.connectionCapabilities = Connection.CAPABILITY_SUPPORT_HOLD or Connection.CAPABILITY_MUTE
        controller.startOutgoingCall("${request.address.scheme}:${request.address.schemeSpecificPart}")
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
        Log.d(TAG, "phoneAccount ${connectionManagerPhoneAccount}")
        Log.d(TAG, "request.address ${request.address}")
        Log.d(TAG, "request.address.scheme ${request.address.scheme}")
        Log.d(TAG, "request.address.host ${request.address.host}")
        Log.d(TAG, "request.address.schemeSpecificPart ${request.address.schemeSpecificPart}")

        val callId = request.extras.getInt(STRING_KEY_PJSUA_CALL_ID)
        val pjsuaCall = pjsuaManager.getCall(callId)
            ?: return Connection.createFailedConnection(
                DisconnectCause(DisconnectCause.ERROR))

        val controller = Pjsua2SipCallController(pjsuaManager)
        controller.setCall(pjsuaCall)
        val sipConnection = SipConnection(
            controller,
            request.address
        )
        return sipConnection
    }
}