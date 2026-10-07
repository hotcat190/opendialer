package com.samsung.sip

interface SipCallListener {
    fun onCalling()

    fun onRinging()

    fun onConnected()

    fun onDisconnected(cause: Int)

    fun onHeld()

    fun onResumed()
}