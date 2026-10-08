package com.samsung.sip.callcontroller

interface SipCallController {
    fun setListener(listener: Listener)
    fun clearListener()
    fun startOutgoingCall(destination: String)
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
        fun onDisconnected(cause: Int)
    }
}