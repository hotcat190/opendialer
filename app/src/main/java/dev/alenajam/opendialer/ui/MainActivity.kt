package dev.alenajam.opendialer.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.alenajam.opendialer.core.common.DefaultPhoneManager
import dev.alenajam.opendialer.feature.appShell.DialerApp
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var defaultPhoneManager: DefaultPhoneManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }


        // Source - https://stackoverflow.com/a/49858933
        // Posted by TALE
        // Retrieved 2026-10-07, License - CC BY-SA 3.0
        val intent = Intent()
        intent.setComponent(
            ComponentName(
                "com.android.server.telecom",
                "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
            )
        )
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        setContent {
            DialerApp(defaultPhoneManager = defaultPhoneManager)
        }
    }
}
