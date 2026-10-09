package com.samsung.sip

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.samsung.sip.SipNotification.NOTIFICATION_ID
import com.samsung.sip.callcontroller.SipIncomingCallCoordinator
import com.samsung.sip.pjsua.PjsuaManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SipForegroundService : Service() {
    @Inject
    lateinit var pjsuaManager: PjsuaManager

    override fun onCreate() {
        super.onCreate()

        SipNotification.createChannel(this)
        val notification = SipNotification.createNotification(this)

        startForeground(
            NOTIFICATION_ID,
            notification
        )

        pjsuaManager.initialize()
        pjsuaManager.setIncomingCallListener(
            SipIncomingCallCoordinator(this)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        pjsuaManager.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}