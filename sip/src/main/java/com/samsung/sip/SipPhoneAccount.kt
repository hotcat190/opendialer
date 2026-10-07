package com.samsung.sip

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.annotation.RequiresPermission

object SipPhoneAccount {
    private val TAG = "SipPhoneAccount"

    fun getHandle(context: Context): PhoneAccountHandle {
        return PhoneAccountHandle(
            ComponentName(
                context,
                SipConnectionService::class.java
            ),
            "PJSIP"
        )
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun register(context: Context) {
        Log.d(TAG, "SipPhoneAccount register start")
        val telecomManager = context.getSystemService(TelecomManager::class.java)
        Log.d(TAG, "${telecomManager.callCapablePhoneAccounts.size}")
        val handle = getHandle(context)

        val account = PhoneAccount.builder(
            handle,
            "PJSIP"
        ).setCapabilities(
            PhoneAccount.CAPABILITY_CALL_PROVIDER
        ).setSupportedUriSchemes(
            listOf(PhoneAccount.SCHEME_SIP)
        ).build()

        telecomManager.registerPhoneAccount(account)
        Log.d(TAG, "SipPhoneAccount registered OK")
        Log.d(TAG, "${telecomManager.callCapablePhoneAccounts.size}")
        Log.d(TAG, "${account.isEnabled}")
    }
}