package dev.alenajam.opendialer;

import android.app.Application;

import com.samsung.sip.SipPhoneAccount;
import com.samsung.sip.pjsua.PjsuaManager;

import dagger.hilt.android.HiltAndroidApp;
import dev.alenajam.opendialer.helper.NotificationHelper;
import dev.alenajam.opendialer.core.common.SharedPreferenceHelper;

@HiltAndroidApp
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.setupNotificationChannels(this);
        SharedPreferenceHelper.init(this);
        SipPhoneAccount.INSTANCE.register(this);
    }
}
