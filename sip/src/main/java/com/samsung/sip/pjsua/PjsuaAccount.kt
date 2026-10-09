package com.samsung.sip.pjsua

import android.util.Log
import org.pjsip.pjsua2.Account
import org.pjsip.pjsua2.OnIncomingCallParam

internal class PjsuaAccount(
    private val manager: PjsuaManager
) : Account() {
    companion object {
        private val TAG = PjsuaAccount::class.simpleName
    }

    override fun onIncomingCall(prm: OnIncomingCallParam) {
        Log.d(TAG, "$TAG onIncomingCall()")
        val call = PjsuaCall(
            manager = manager,
            account = this,
            callId = prm.callId,
        )
        manager.onIncomingCall(call)
    }
}