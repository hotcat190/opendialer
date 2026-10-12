package com.samsung.sip.callcontroller

import android.telecom.DisconnectCause
import com.samsung.sip.pjsua.PjsuaCall
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsip_status_code
import org.pjsip.pjsua2.pjsua_call_flag

class Pjsua2SipCallController (
    private var call: PjsuaCall
) : SipCallController, PjsuaCall.Listener {

    private var listener: SipCallController.Listener? = null

    override fun setListener(listener: SipCallController.Listener) {
        this.listener = listener
    }

    override fun clearListener() {
        listener = null
    }

    override fun onPjsuaStateChanged(state: Int) {
        when (state) {
            pjsip_inv_state.PJSIP_INV_STATE_CALLING -> listener?.onDialing()
            pjsip_inv_state.PJSIP_INV_STATE_INCOMING -> {
                call?.answer(CallOpParam().apply {
                    statusCode = pjsip_status_code.PJSIP_SC_PROGRESS
                })
                listener?.onRinging()
            }
            pjsip_inv_state.PJSIP_INV_STATE_EARLY -> {

            }
            pjsip_inv_state.PJSIP_INV_STATE_CONNECTING -> {
            }
            pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> listener?.onActive()
            pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> listener?.onDisconnected(
                DisconnectCause.REMOTE
            )
        }
    }

    override fun answer() {
        call?.answer(CallOpParam().apply {
            statusCode = pjsip_status_code.PJSIP_SC_OK
        })
    }

    override fun reject() {
        call?.hangup(CallOpParam(true))
        listener?.onDisconnected(DisconnectCause.LOCAL)
        clearListener()
    }

    override fun hangup() {
        call?.hangup(CallOpParam(true))
        listener?.onDisconnected(DisconnectCause.LOCAL)
        clearListener()
    }

    override fun hold() {
        call?.setHold(CallOpParam(true))
        listener?.onHolding()
    }

    override fun resume() {
        call?.reinvite(CallOpParam(true).apply {
            opt.flag = pjsua_call_flag.PJSUA_CALL_UNHOLD.toLong()
        })
        listener?.onActive()
    }
}