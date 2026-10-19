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

    override fun onPjsuaStateChanged(state: Int, statusCode: Int) {
        when (state) {
            pjsip_inv_state.PJSIP_INV_STATE_CALLING -> listener?.onDialing()
            pjsip_inv_state.PJSIP_INV_STATE_INCOMING -> {
                call?.answer(CallOpParam().apply {
                    setStatusCode(pjsip_status_code.PJSIP_SC_RINGING)
                })
                listener?.onRinging()
            }
            pjsip_inv_state.PJSIP_INV_STATE_EARLY -> {

            }
            pjsip_inv_state.PJSIP_INV_STATE_CONNECTING -> {
            }
            pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> listener?.onActive()
            pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> {
                val cause = when (statusCode) {
                    pjsip_status_code.PJSIP_SC_BUSY_HERE
                        -> DisconnectCause(
                        DisconnectCause.REMOTE,
                        "Remote busy",
                        "SIP code $statusCode",
                        null
                    )

                    pjsip_status_code.PJSIP_SC_REQUEST_TERMINATED,
                    pjsip_status_code.PJSIP_SC_TEMPORARILY_UNAVAILABLE
                        -> DisconnectCause(
                        DisconnectCause.REMOTE,
                        "Remote unavailable",
                        "SIP code $statusCode",
                        null
                    )
                    else -> DisconnectCause(
                        DisconnectCause.REMOTE,
                        "Remote hung up",
                        "Normal BYE after connect",
                        null
                    )
                }
                listener?.onDisconnected(
                    cause
                )
            }
        }
    }

    override fun answer() {
        call?.answer(CallOpParam().apply {
            statusCode = pjsip_status_code.PJSIP_SC_OK
        })
    }

    override fun reject() {
        listener?.onDisconnected(DisconnectCause(
            DisconnectCause.REJECTED,
            "Declined",
            "User rejected incoming call",
            null
        ))
        clearListener()
        call?.hangup(CallOpParam(true))
    }

    override fun hangup() {
        listener?.onDisconnected(DisconnectCause(
            DisconnectCause.LOCAL,
            "Disconnected",
            "User hangup ongoing call",
            null
        ))
        clearListener()
        call?.hangup(CallOpParam(true))
    }

    override fun hold() {
        listener?.onHolding()
        call?.setHold(CallOpParam(true))
    }

    override fun resume() {
        listener?.onActive()
        call?.reinvite(CallOpParam(true).apply {
            opt.flag = pjsua_call_flag.PJSUA_CALL_UNHOLD.toLong()
        })
    }
}