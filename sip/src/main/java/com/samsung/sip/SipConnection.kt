package com.samsung.sip

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.telecom.TelecomManager.PRESENTATION_ALLOWED
import android.util.Log

class SipConnection(
    private val context: Context,
    private val destination: Uri?
) : Connection() {
    private val TAG = "SipConnection"

    private val handler = Handler(Looper.getMainLooper())

    init {
        setInitializing()
        Log.d(TAG, "SipConnection initializing")
        setAddress(destination, PRESENTATION_ALLOWED)
        audioModeIsVoip = true
        connectionCapabilities = CAPABILITY_MUTE or CAPABILITY_SUPPORT_HOLD or CAPABILITY_HOLD
        setInitialized()
        Log.d(TAG, "SipConnection initialized")
    }

    override fun onAnswer() {
        Log.d(TAG, "SipConnection onAnswer")
        super.onAnswer()
        setActive()
    }

    override fun onDisconnect() {
        Log.d(TAG, "SipConnection onDisconnect")
        super.onDisconnect()
        disconnect()
    }

    override fun onHold() {
        Log.d(TAG, "SipConnection onHold")
        super.onHold()
        setOnHold()
    }

    override fun onReject() {
        Log.d(TAG, "SipConnection onReject")
        super.onReject()
        disconnect()
    }

    override fun onUnhold() {
        Log.d(TAG, "SipConnection onUnhold")
        super.onUnhold()
        setActive()
    }

    fun startFakeOutgoingCall() {
        Log.d(TAG, "SipConnection startFakeOutgoingCall")
        setDialing()

        handler.postDelayed({
            if (state == STATE_DIALING) {
                Log.d(TAG, "SipConnection faking answer by setActive()")
                setActive()
            }
        }, 2000)
    }

    fun startFakeIncomingCall() {
        Log.d(TAG, "SipConnection startFakeIncomingCall")
        setRinging()
    }

    private fun disconnect() {
        Log.d(TAG, "SipConnection disconnect")
        if (state == STATE_DISCONNECTED) {
            return
        }
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        handler.removeCallbacksAndMessages(null)
        destroy()
        Log.d(TAG, "SipConnection destroyed")
    }
}