package dev.alenajam.opendialer.core.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPreferenceHelper {
    const val SP_QUICK_RESPONSES = "SP_QUICK_RESPONSES"
    const val KEY_SETTING_THEME = "theme"
    const val KEY_SETTING_DEFAULT = "default"
    const val KEY_SETTING_SOUND_VIBRATION = "sound"
    const val KEY_SETTING_QUICK_RESPONSES = "quick_responses"
    const val KEY_SETTING_BLOCKED_NUMBERS = "blockedNumbers"
    const val KEY_SETTING_NOTIFICATION_SETTINGS = "notificationSettings"
    const val KEY_SETTING_UPDATE_CHECKS = "update_checks"
    private const val KEY_CALL_LOG_FAVORITES_EXPANDED = "call_log_favorites_expanded"
    private const val KEY_DISMISSED_UPDATE_VERSION = "dismissed_update_version"
    private const val KEY_UPDATE_LAST_CHECK_MILLIS = "update_last_check_millis"
    private const val KEY_UPDATE_ETAG = "update_etag"
    private const val KEY_AVAILABLE_UPDATE_VERSION = "available_update_version"
    private const val KEY_AVAILABLE_UPDATE_URL = "available_update_url"

    const val SIP_PASSWORD = "sip_password"
    const val SIP_ACC_DOMAIN = "sip_acc_domain"
    const val SIP_ACC_USER = "sip_acc_user"

    const val SIP_LISTENING_PORT = "sip_listening_port"

    fun setSipPort(context: Context, port : String){
        getSecurePrefs(context).edit{
            putString(SIP_LISTENING_PORT, port)
        }
    }

    fun getSipPort(context: Context): String? {
        return getSecurePrefs(context).getString(SIP_LISTENING_PORT, "")
    }

    fun setSipDomain(context: Context, domain : String){
        getSecurePrefs(context).edit{
            putString(SIP_ACC_DOMAIN, domain)
        }
    }

    fun getSipDomain(context: Context): String? {
        return getSecurePrefs(context).getString(SIP_ACC_DOMAIN, "")
    }

    fun setSipUser(context: Context, user : String){
        getSecurePrefs(context).edit{
            putString(SIP_ACC_USER, user)
        }
    }

    fun getSipUser(context: Context): String?{
        return getSecurePrefs(context).getString(SIP_ACC_USER, "")
    }

    fun setSipPassword(context: Context, password: String) {
        getSecurePrefs(context).edit {
            putString(SIP_PASSWORD, password)
        }
    }

    fun getSipPassword(context: Context): String? {
        return getSecurePrefs(context).getString(SIP_PASSWORD, "")
    }
    @Volatile
    private var securePrefs: SharedPreferences ? = null
    enum class ThemeMode(val nightMode: Int) {
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES),
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        companion object {
            fun from(nightMode: Int) = entries.firstOrNull { it.nightMode == nightMode } ?: SYSTEM
        }
    }

    private fun getSecurePrefs(context: Context): SharedPreferences {
        return securePrefs ?: synchronized(this) {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            securePrefs = EncryptedSharedPreferences.create(
                context,
                "secure_sip_prefs", // file name
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            securePrefs!!
        }
    }




    @JvmStatic
    fun getSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @JvmStatic
    fun init(context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        val theme = sharedPreferences.getString(KEY_SETTING_THEME, null)
        try {
            CommonUtils.setTheme(if (theme == null) ThemeMode.SYSTEM.nightMode else theme.toInt())
        } catch (e: NumberFormatException) {
            Log.d(SharedPreferenceHelper::class.java.simpleName, e.localizedMessage ?: "")
        }

        if (!sharedPreferences.contains(SP_QUICK_RESPONSES)) {
            val quickResponses = context.resources.getStringArray(R.array.array_quick_responses)
            val quickResponseList = quickResponses.toMutableList()
            sharedPreferences.edit().putString(SP_QUICK_RESPONSES, Gson().toJson(quickResponseList)).apply()
        }
    }

    fun getQuickResponses(context: Context): List<String> {
        val fallback = context.resources.getStringArray(R.array.array_quick_responses).toList()
        val json = getSharedPreferences(context).getString(SP_QUICK_RESPONSES, null)
            ?: return fallback

        return runCatching {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson<List<String>>(json, type)
                ?.map(String::trim)
                ?.filter(String::isNotEmpty)
                ?.takeIf { it.isNotEmpty() }
                ?: fallback
        }.getOrDefault(fallback)
    }

    fun saveQuickResponses(context: Context, responses: List<String>) {
        val cleanedResponses = responses.map(String::trim).filter(String::isNotEmpty)
        getSharedPreferences(context)
            .edit()
            .putString(SP_QUICK_RESPONSES, Gson().toJson(cleanedResponses))
            .apply()
    }

    fun getThemeMode(context: Context): ThemeMode {
        val savedNightMode = getSharedPreferences(context)
            .getString(KEY_SETTING_THEME, null)
            ?.toIntOrNull()
        return ThemeMode.from(savedNightMode ?: ThemeMode.SYSTEM.nightMode)
    }

    fun setThemeMode(context: Context, themeMode: ThemeMode) {
        getSharedPreferences(context)
            .edit()
            .putString(KEY_SETTING_THEME, themeMode.nightMode.toString())
            .apply()
        CommonUtils.setTheme(themeMode.nightMode)
    }

    fun isCallLogFavoritesExpanded(context: Context): Boolean =
        getSharedPreferences(context).getBoolean(KEY_CALL_LOG_FAVORITES_EXPANDED, true)

    fun setCallLogFavoritesExpanded(context: Context, expanded: Boolean) {
        getSharedPreferences(context)
            .edit()
            .putBoolean(KEY_CALL_LOG_FAVORITES_EXPANDED, expanded)
            .apply()
    }

    fun isUpdateCheckEnabled(context: Context): Boolean =
        getSharedPreferences(context).getBoolean(KEY_SETTING_UPDATE_CHECKS, true)

    fun setUpdateCheckEnabled(context: Context, enabled: Boolean) {
        getSharedPreferences(context)
            .edit()
            .putBoolean(KEY_SETTING_UPDATE_CHECKS, enabled)
            .apply()
    }

    fun getDismissedUpdateVersion(context: Context): String? =
        getSharedPreferences(context).getString(KEY_DISMISSED_UPDATE_VERSION, null)

    fun setDismissedUpdateVersion(context: Context, version: String) {
        getSharedPreferences(context)
            .edit()
            .putString(KEY_DISMISSED_UPDATE_VERSION, version)
            .apply()
    }

    fun getUpdateLastCheckMillis(context: Context): Long =
        getSharedPreferences(context).getLong(KEY_UPDATE_LAST_CHECK_MILLIS, 0L)

    fun setUpdateLastCheckMillis(context: Context, timestampMillis: Long) {
        getSharedPreferences(context)
            .edit()
            .putLong(KEY_UPDATE_LAST_CHECK_MILLIS, timestampMillis)
            .apply()
    }

    fun getUpdateEtag(context: Context): String? =
        getSharedPreferences(context).getString(KEY_UPDATE_ETAG, null)

    fun setUpdateEtag(context: Context, etag: String?) {
        getSharedPreferences(context)
            .edit()
            .putString(KEY_UPDATE_ETAG, etag)
            .apply()
    }

    fun getAvailableUpdateVersion(context: Context): String? =
        getSharedPreferences(context).getString(KEY_AVAILABLE_UPDATE_VERSION, null)

    fun getAvailableUpdateUrl(context: Context): String? =
        getSharedPreferences(context).getString(KEY_AVAILABLE_UPDATE_URL, null)

    fun setAvailableUpdate(context: Context, version: String?, url: String?) {
        getSharedPreferences(context)
            .edit()
            .putString(KEY_AVAILABLE_UPDATE_VERSION, version)
            .putString(KEY_AVAILABLE_UPDATE_URL, url)
            .apply()
    }
}
