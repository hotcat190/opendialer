package com.samsung.sip

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.telecom.TelecomManager.PRESENTATION_ALLOWED
import android.util.Log
import com.samsung.sip.callcontroller.SipCallController

class SipConnection(
    private val callController: SipCallController,
    address: Uri
) : Connection(), SipCallController.Listener {
    private val TAG = "SipConnection"

    init {
        Log.d(TAG, "SipConnection init{}")
        setInitializing()
        Log.d(TAG, "before ${address.toString()}")
        setAddress(address, PRESENTATION_ALLOWED)
        Log.d(TAG, "after ${address.toString()}")
        audioModeIsVoip = true
        connectionCapabilities = CAPABILITY_MUTE or CAPABILITY_SUPPORT_HOLD or CAPABILITY_HOLD

        callController.setListener(this)
    }

    override fun onAnswer() {
        Log.d(TAG, "SipConnection onAnswer")
        callController.answer()
    }

    override fun onDisconnect() {
        Log.d(TAG, "SipConnection onDisconnect")
        callController.hangup()
    }

    override fun onHold() {
        Log.d(TAG, "SipConnection onHold")
        callController.hold()
    }

    override fun onReject() {
        Log.d(TAG, "SipConnection onReject")
        callController.reject()
    }

    override fun onUnhold() {
        Log.d(TAG, "SipConnection onUnhold")
        callController.resume()
    }

    // ---------------------------
    // SipCallController.Listener
    // ---------------------------

    override fun onDialing() {
        setDialing()
    }

    override fun onRinging() {
        setRinging()
    }

    override fun onActive() {
        setActive()
    }

    override fun onHolding() {
        setOnHold()
    }

    override fun onDisconnected(cause: Int) {
        setDisconnected(DisconnectCause(cause))
        callController.clearListener()
        destroy()
    }
}