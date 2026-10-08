package com.samsung.sip.pjsua

import org.pjsip.pjsua2.Account
import org.pjsip.pjsua2.OnIncomingCallParam

internal class PjsuaAccount(
    private val manager: PjsuaManager
) : Account() {
    override fun onIncomingCall(prm: OnIncomingCallParam) {
        val call = PjsuaCall(
            manager = manager,
            account = this,
            callId = prm.callId
        )
        manager.onIncomingCall(call)
    }
}