package com.samsung.sip.callcontroller

import android.os.Handler
import android.os.Looper
import android.telecom.DisconnectCause
import android.util.Log

class FakeSipCallController : SipCallController {
    private companion object {
        val TAG = FakeSipCallController::class.simpleName
    }
    private val handler = Handler(Looper.getMainLooper())
    private var listener: SipCallController.Listener? = null
    private var disconnected: Boolean = false

    override fun setListener(listener: SipCallController.Listener) {
        this.listener = listener
    }

    override fun clearListener() {
        this.listener = null
    }

    override fun startOutgoingCall(destination: String) {
        disconnected = false
        listener?.onDialing()
        handler.postDelayed({
            if (!disconnected) {
                Log.d(TAG, "FakeSipCallController faking answer by listener.onActive()")
                listener?.onActive()
            }
        }, 2000)
    }

    fun simulateIncomingCall() {
        disconnected = false
        listener?.onRinging()
    }

    override fun answer() {
        Log.d(TAG, "$TAG answer")
        if (disconnected) {
            return
        }
        listener?.onActive()
    }

    override fun reject() {
        disconnect()
    }

    override fun hangup() {
        disconnect()
    }

    override fun hold() {
        listener?.onHolding()
    }

    override fun resume() {
        listener?.onActive()
    }

    private fun disconnect() {
        Log.d(TAG, "FakeSipCallController disconnect")
        if (disconnected) {
            return
        }
        disconnected = true
        handler.removeCallbacksAndMessages(null)
        listener?.onDisconnected(DisconnectCause.LOCAL)
    }
}