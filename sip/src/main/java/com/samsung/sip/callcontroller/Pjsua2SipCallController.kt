package com.samsung.sip.callcontroller

import android.telecom.DisconnectCause
import com.samsung.sip.pjsua.PjsuaCall
import com.samsung.sip.pjsua.PjsuaManager
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.pjsip_inv_state

class Pjsua2SipCallController (
    private val pjsuaManager: PjsuaManager
) : SipCallController {

    private var listener: SipCallController.Listener? = null
    private var call: PjsuaCall? = null

    override fun setListener(listener: SipCallController.Listener) {
        this.listener = listener
    }

    override fun clearListener() {
        listener = null
    }

    fun onPjuaStateChangedImpl(state: Int) {
        when (state) {
            pjsip_inv_state.PJSIP_INV_STATE_CALLING -> listener?.onDialing()
            pjsip_inv_state.PJSIP_INV_STATE_INCOMING -> listener?.onRinging()
            pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> listener?.onActive()
            pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> listener?.onDisconnected(
                DisconnectCause.REMOTE
            )
        }
    }

    override fun startOutgoingCall(destination: String) {
        call = pjsuaManager.makeCall(destination)
        call!!.setListener(object : PjsuaCall.Listener {
            override fun onPjsuaStateChanged(state: Int)
                = onPjuaStateChangedImpl(state)
        })
    }

    fun setCall(call: PjsuaCall) {
        this.call = call
        call.setListener(object : PjsuaCall.Listener {
            override fun onPjsuaStateChanged(state: Int)
                = onPjuaStateChangedImpl(state)
        })
    }

    override fun answer() {
        call?.answer(CallOpParam(true))
    }

    override fun reject() {
        call?.hangup(CallOpParam(true))
    }

    override fun hangup() {
        call?.hangup(CallOpParam(true))
    }

    override fun hold() {
        call?.setHold(CallOpParam(true))
    }

    override fun resume() {
        call?.reinvite(CallOpParam(true))
    }
}