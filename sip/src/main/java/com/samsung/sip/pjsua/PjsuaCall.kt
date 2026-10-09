package com.samsung.sip.pjsua

import android.util.Log
import org.pjsip.pjsua2.Account
import org.pjsip.pjsua2.Call
import org.pjsip.pjsua2.OnCallMediaStateParam
import org.pjsip.pjsua2.OnCallStateParam
import org.pjsip.pjsua2.pjmedia_type
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsua_call_media_status

class PjsuaCall(
    account: Account,
    val callId: Int,
    private val manager: PjsuaManager,
) : Call(account, callId) {
    companion object {
        private val TAG = PjsuaCall::class.simpleName
    }

    interface Listener {
        fun onPjsuaStateChanged(state: Int)
    }

    private var listener: Listener? = null

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun clearListener() {
        listener = null
    }


    override fun onCallState(prm: OnCallStateParam?) {
        val ci = try {
            info
        } catch (e: Exception) {
            println("Failed getting call info: $e")
            e.printStackTrace()
            return
        }

        println(
            "PJSUA2 call state: " +
            "${ci.state} ${ci.stateText}"
        )

        listener?.onPjsuaStateChanged(ci.state)

        if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            manager.removeCall(this)
        }
    }

    override fun onCallMediaState(prm: OnCallMediaStateParam?) {
        val ci = try {
            info
        } catch (e: Exception) {
            Log.d(TAG, "Exception getting call info: $e")
            e.printStackTrace()
            return
        }
        val cmiv = ci.media
        for (i in cmiv.indices) {
            val cmi = cmiv[i]
            if (cmi.type == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                (
                        cmi.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE ||
                                cmi.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD)
            ) {
                /* Connect ports */
                try {
                    val audioMedia = getAudioMedia(i)
                    manager.connectAudio(audioMedia)
                } catch (e: Exception) {
                    println("Failed connecting media ports $e")
                    e.printStackTrace()
                }
            }
        }
    }
}