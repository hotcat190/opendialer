package com.samsung.sip.callcontroller

import android.telecom.DisconnectCause

interface SipCallController {
    fun setListener(listener: Listener)
    fun clearListener()
    fun answer()
    fun reject()
    fun hangup()
    fun hold()
    fun resume()

    interface Listener {
        fun onDialing()
        fun onRinging()
        fun onActive()
        fun onHolding()
        fun onDisconnected(cause: DisconnectCause)
    }
}