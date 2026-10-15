package dev.alenajam.opendialer.feature.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.samsung.sip.pjsua.PjsuaManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.alenajam.opendialer.core.common.SharedPreferenceHelper
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    @ApplicationContext private  var context: Context,
    private var pjsuaManager: PjsuaManager
): ViewModel (){
    private val _password = MutableStateFlow("")
    private val _domain = MutableStateFlow("")
    private val _user = MutableStateFlow("")
    private val _port = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    val domain: StateFlow<String> = _domain.asStateFlow()
    val user: StateFlow<String> = _user.asStateFlow()
    val port: StateFlow<String> = _port.asStateFlow()

    init {
        _password.value = SharedPreferenceHelper.getSipPassword(context).toString()

        _domain.value = SharedPreferenceHelper.getSipDomain(context).toString()

        _user.value = SharedPreferenceHelper.getSipUser(context).toString()

        _port.value = SharedPreferenceHelper.getSipPort(context).toString()
    }


    fun updatePassword(newPassword : String){
        if(newPassword == _password.value){
            return
        }

        _password.value = newPassword
    }

    fun updateDomain(newDomain : String){
        if(newDomain == _domain.value){
            return
        }

        _domain.value = newDomain

    }

    fun updateUser(newUser: String){
        if(newUser == _user.value){
            return
        }

        _user.value = newUser
    }

    fun updatePort(newPort: String){
        if(newPort == _port.value){
            return
        }

        _port.value = newPort
    }

    fun savePort(){
        SharedPreferenceHelper.setSipPort(context, _port.value)
    }
    fun savePassword(){
        SharedPreferenceHelper.setSipPassword(context, _password.value)
    }

    fun saveDomain(){
        SharedPreferenceHelper.setSipDomain(context, _domain.value)
    }

    fun saveUser(){
        SharedPreferenceHelper.setSipUser(context, _user.value)
    }

    fun saveAll(){
        saveDomain()
        saveUser()
        savePassword()
        savePort()

        pjsuaManager.reconnect()
    }


}