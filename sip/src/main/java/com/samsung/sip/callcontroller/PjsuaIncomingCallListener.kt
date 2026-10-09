package com.samsung.sip.callcontroller

import com.samsung.sip.pjsua.PjsuaCall

interface PjsuaIncomingCallListener {
    fun onIncomingCall(call: PjsuaCall)
}