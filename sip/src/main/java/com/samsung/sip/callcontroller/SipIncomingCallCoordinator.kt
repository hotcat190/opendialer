package com.samsung.sip.callcontroller

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.telecom.TelecomManager.EXTRA_INCOMING_CALL_ADDRESS
import android.util.Log
import com.samsung.sip.SipPhoneAccount
import com.samsung.sip.pjsua.PjsuaCall
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

@Singleton
class SipIncomingCallCoordinator @Inject constructor(
    @ApplicationContext private val context: Context
) : PjsuaIncomingCallListener {
    companion object {
        private val TAG = SipIncomingCallCoordinator::class.simpleName
        const val STRING_KEY_PJSUA_CALL_ID = "pjsua_call_id"
    }

    override fun onIncomingCall(call: PjsuaCall) {
        Log.d(TAG, "$TAG onIncomingCall")
        Log.d(TAG,"localUri=${call.info.localUri}")
        Log.d(TAG, "localContact=${call.info.localContact}")
        Log.d(TAG, "remoteUri=${call.info.remoteUri}")
        Log.d(TAG,"remoteContact=${call.info.remoteContact}")
        Log.d(TAG, "remoteUri.toUri()=${call.info.remoteUri.toUri()}")
        Log.d(TAG, "Uri.parse(remoteUri)=${Uri.parse(call.info.remoteUri)}")
        val telecomManager = context.getSystemService(TelecomManager::class.java)
        val extras = Bundle().apply {
            putInt(STRING_KEY_PJSUA_CALL_ID, call.callId)
            putParcelable(EXTRA_INCOMING_CALL_ADDRESS, call.info.remoteUri.toUri())
        }
        telecomManager.addNewIncomingCall(
            SipPhoneAccount.getHandle(context),
            extras
        )
    }
}